(ns ordnerd.handler
  (:require [clojure.string :as str]
            [clojure.core.async :refer [go]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [cheshire.core :refer :all]
            [environ.core :refer [env]]
            [clj-http.client :as client]
            [clj-http.util :as util]
            [ordnerd.dictionary.swedish :as swe]
            [ordnerd.markdown :as markdown]
            [ordnerd.stats :as stats]
            ))

(def webhook-uid
  (or (env :webhook) "WEBHOOK"))

(def token
  (or (env :token) "NOT-SET"))

(defn telegram-send-message
  [chat-id text]
  (if-not (some? chat-id)
    (println "UNKNOWN CHAT ID")
    (let
      [url-encoded-text (util/url-encode text)]
      (->
        "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&parse_mode=Markdown&text=%s"
        (format token chat-id url-encoded-text)
        (client/get {:throw-exceptions true})))))

(defn- if-blank
  "If s is nil, empty, or contains only whitespace evaluates then, if not, yields else"
  ([^CharSequence s then]
   (if-blank s then nil))
  ([^CharSequence s then else]
   (if (str/blank? s) then else)))

(defn- blank-to-nil
  "If s is nil, empty, or contains only whitespace return nil, if not, yields s"
  [^CharSequence s]
  (if-blank s nil s))

(defn- lexeme->text
  [lexeme]
  (let
    [definition-text (if-some [definition (->
                                            lexeme
                                            (:definition)
                                            (blank-to-nil))]
                       (str \newline
                            "Betydelse:"
                            \newline
                            (markdown/bold definition))
                       "")

     usage-text (if-some [usage (->
                                  lexeme
                                  (:usage)
                                  (blank-to-nil))]
                  (str \newline
                       "Användning:"
                       \newline
                       (markdown/italic usage))
                  "")

     example (if-some [example (:example lexeme)]
               (markdown/italic example)
               (->>
                 (:examples lexeme)
                 (filter #(not (str/blank? %)))
                 (map markdown/italic)
                 (str/join \newline)))
     example-text (if (str/blank? example)
                    ""
                    (str \newline
                         "Exempel:"
                         \newline
                         example))]
    (str definition-text
         usage-text
         example-text)))

(defn word->text
  [word]
  (let
    [form-text (->
                 (:form word)
                 (.replaceAll "~" "")
                 (markdown/bold))

     pos-text (if-some [pos (:pos word)]
                (->>
                  (str "[" pos "]")
                  (markdown/italic)
                  (str " "))
                "")

     inflections-text (->>
                        (:inflections word)
                        (str/join " ")
                        (markdown/fixed))

     lexemes-text (->>
                    (:lexeme word)
                    (map lexeme->text)
                    (str/join \newline))]
    (str \newline
         form-text pos-text
         \newline
         inflections-text
         \newline
         \newline
         lexemes-text)))

(defn dont-know-text
  [query]
  (str "Jag tyvärr känner inte till det ordet: " (markdown/bold query)))

(def no-message-text
  (str "Jag tyvärr ser ingen text meddelande. \uD83D\uDE14"))

(defn greeting-text
  [user-first-name]
  (str "Hejsan"
       (if (str/blank? user-first-name)
         "! "
         (str " " user-first-name "! "))
       "Jag heter Ordnerd. Jag kan förklara ordets betydelse. Skriv bara ett ord och du få se!"))

(defn webhook-endpoint
  [update]
  (let
    [message-id (get-in update [:message :message-id])
     chat-id (get-in update [:message :chat :id])
     message-text (get-in update [:message :text])
     user-first-name (get-in update [:message :from :first_name])]
    (println "INCOMING UPDATE" "-->" update)
    (if (str/blank? message-text)
              (telegram-send-message chat-id no-message-text)
              (do
                (case
                  message-text
                  "/start" (telegram-send-message chat-id (greeting-text user-first-name))
                  "/slumpa" (telegram-send-message chat-id (-> (swe/random) (word->text)))
                  (let
                    [query (->
                             message-text
                             (or "")
                             (.toLowerCase))
                     words (->
                             query
                             (swe/search))
                     text (if (empty? words)
                            (do
                              ;(stats/publish-event-zerohits-async query)
                              (dont-know-text query))
                            (->>
                              words
                              (map word->text)
                              (str/join \newline)))]
                    (telegram-send-message chat-id text)))))))

(defroutes app-routes

           (GET "/"
                []
             "OK")

           (GET "/dictionary/swedish/:word"
                [word]
             (let
               [words (swe/search word)]
               (if (empty? words)
                 {:status  204
                  :headers {"Content-Type" "application/json; charset=utf-8"}}
                 {:status  200
                  :body    (->
                             words
                             (first)
                             (generate-string))
                  :headers {"Content-Type" "application/json; charset=utf-8"}})))

           (POST (str "/bot/telegram/" webhook-uid)
                 {body :body}
             (let
               [payload (try
                          (-> body
                              (slurp :encoding "utf-8")
                              (parse-string true))
                          (catch Exception e nil))]
               (if (nil? payload)
                 {:status 400 :headers {"Content-Type" "application/json; charset=utf-8"}}
                 (do
                   (go (webhook-endpoint payload))
                   {:status 202 :headers {"Content-Type" "application/json; charset=utf-8"}}))))

           (route/not-found "NOWHERE TO BE FOUND"))

(def app
  (wrap-defaults app-routes api-defaults))

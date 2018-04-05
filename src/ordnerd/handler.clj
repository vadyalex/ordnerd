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
            [ordnerd.markdown :as markdown]))

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

(defn- lexeme->text
  [lexeme]
  (let
    [definition (:definition lexeme)
     definition-text (if (str/blank? definition)
                       ""
                       (str \newline
                            "Betydelse:"
                            \newline
                            (markdown/bold definition)))

     usage (:usage lexeme)
     usage-text (if (str/blank? usage)
                  ""
                  (str \newline
                       "Användning:"
                       \newline
                       (markdown/italic usage)))

     example (if (contains? lexeme :example)
               (markdown/italic (:example lexeme))
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
    (case
      message-text
      "/start" (telegram-send-message chat-id (greeting-text user-first-name))
      "/slumpa" (telegram-send-message chat-id (-> (swe/random) (word->text)))
      (let
        [query (->
                 message-text
                 (or "")
                 (.toLowerCase)
                 (str/split #" ")
                 (first))
         words (->
                 query
                 (swe/search))
         text (if (empty? words)
                (dont-know-text query)
                (->>
                  words
                  (map word->text)
                  (str/join \newline)))]
        (telegram-send-message chat-id text)))
    ))

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

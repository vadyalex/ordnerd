(ns ordnerd.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ordnerd.dictionary.swedish :as swe]
            [ordnerd.markdown :as markdown]
            [cheshire.core :refer :all]
            [environ.core :refer [env]]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [clj-http.client :as client]
            [clj-http.util :as util]))

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

(defn lexeme->text
  [lexeme]
  (let
    [definition-text (:definition lexeme)
     usage-text (:usage lexeme)
     example-text (if (contains? lexeme :example)
                    (markdown/italic (get lexeme :examples))
                    (->>
                      (get lexeme :examples)
                      (filter #(not (str/blank? %)))
                      (map markdown/italic)
                      (str/join \newline)))]
    (str (if (str/blank? definition-text)
           ""
           (str \newline
                "Betydelse:"
                \newline
                (markdown/bold definition-text)))
         (if (str/blank? usage-text)
           ""
           (str \newline
                "Användning:"
                \newline
                (markdown/italic usage-text)))
         (if (str/blank? example-text)
           ""
           (str \newline
                "Exempel:"
                \newline
                example-text)))))

(defn word->text
  [word]
  (let
    [form-text (->
                 (:form word)
                 (.replaceAll "~" ""))
     inflections-text (->>
                        (:inflections word)
                        (str/join " "))
     lexemes-text (->>
                    (:lexeme word)
                    (map lexeme->text)
                    (str/join \newline))]
    (str \newline
         (markdown/bold form-text)
         \newline
         (markdown/fixed inflections-text)
         \newline
         \newline
         lexemes-text)))

(defn dont-know-text
  [query]
  (str "Jag tyvärr känner inte till det ordet: " (markdown/bold query)))

(defn webhook-endpoint
  [update-event-json-str]
  (let
    [update (parse-string update-event-json-str true)
     message-id (get-in update [:message :message-id])
     chat-id (get-in update [:message :chat :id])
     message-text (get-in update [:message :text])
     query (->
             message-text
             (or "")
             (.toLowerCase)
             (str/split #" ")
             (first))
     word (->
            query
            (swe/search)
            (first))
     text (if (some? word)
            (word->text word)
            (dont-know-text query))]
    (println "INCOMING UPDATE" "-->" update)
    (telegram-send-message chat-id text)
    {:status  202
     :headers {"Content-Type" "application/json; charset=utf-8"}}))

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
             (webhook-endpoint (try
                                 ;; if body is empty slurp will explode..
                                 (slurp body)
                                 (catch Exception e ""))))

           (route/not-found "NOWHERE TO BE FOUND"))

(def app
  (wrap-defaults app-routes api-defaults))

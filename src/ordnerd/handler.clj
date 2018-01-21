(ns ordnerd.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ordnerd.dictionary.swedish :as swe]
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

(defn telegram-answer-dont-know
  [chat-id query]
  (let
    [text (str "Jag tyvärr känner inte till det ordet: *" query "*")]
    (telegram-send-message chat-id text)))

(defn word->markdown
  [word]
  (let
    [form (:form word)
     definition_1 (get-in word [:lexeme :definition])
     definition_2 (get-in word [:lexeme 0 :definition])
     definition (or definition_1 definition_2)]
    (str \newline "*" form "*" \newline \newline "_" definition "_")))

(defn telegram-answer-word
  [chat-id text]
  (telegram-send-message chat-id text))

(defn webhook-endpoint
  [update-json-str]
  (let
    [update (parse-string update-json-str true)
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
            (swe/find-words)
            (first))
     word-found? (some? word)
     answer-message-text (word->markdown word)]
    (println "INCOMING UPDATE" "-->")
    (pprint update)
    (if word-found?
      (telegram-answer-word chat-id answer-message-text)
      (telegram-answer-dont-know chat-id query))
    {:status  202
     :headers {"Content-Type" "application/json; charset=utf-8"}}))

(defroutes app-routes

           (GET "/"
                []
             "OK")

           (GET "/dictionary/swedish/:word"
                [word]
             (let
               [words (swe/find-words word)]
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

(ns ordnerd.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ordnerd.dictionary.swedish :as swe]
            [cheshire.core :refer :all]
            [environ.core :refer [env]]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]))

(def webhook-uid
  (or (env :webhook) "WEBHOOK"))

(def token
  (or (env :token) "NOT-SET"))

(defn webhook-endpoint
  [update-json-str]
  (let
    [update (parse-string update-json-str)
     message-id (get-in update [:message :message-id])
     message-text (get-in update [:message :text])
     word (.toLowerCase (or message-text ""))
     words (swe/find-words word)
     ;answer-message-text (words->message words)]
    (println "INCOMING UPDATE" "-->")
    (pprint update)
    ;(if (empty? words)
    ; (telegram-answer-dont-know message-id)
    ;  (telegram-answer-word message-id answer-message-text))
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

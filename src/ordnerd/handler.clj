(ns ordnerd.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ordnerd.dictionary.swedish :as swe]
            [cheshire.core :refer :all]))

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

           (route/not-found "NOWHERE TO BE FOUND"))

(def app
  (wrap-defaults app-routes api-defaults))

(ns ordnerd.core
  (:require [org.httpkit.server :as server]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]
            [ordnerd.handler :as handler])
  (:gen-class))

(def port
  (Integer. (or (env :port) 5000)))

(defn -main [& args]
  (println "Starting ordnerd on port" port)
  (server/run-server
    handler/app
    {:port port}))

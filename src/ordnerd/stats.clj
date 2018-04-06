(ns ordnerd.stats
  (:require [clojure.string :as str]
            [clojure.core.async :refer [go]]
            [environ.core :refer [env]]
            [taoensso.carmine :as car :refer (wcar)]))

(def redis-uri
  (env :redis))

(def redis-server-conn {:pool {}
                        :spec {:uri redis-uri}})

(defmacro wcar* [& body] `(car/wcar redis-server-conn ~@body))

(def key-stats-event-zerohits "stats:event:zerohits")

(defn publish-event-zerohits
  [query]
  (when-not (str/blank? redis-uri)
    (->
      (car/zadd key-stats-event-zerohits :incr 1 query)
      (wcar*))))

(defn publish-event-zerohits-async
  [query]
  (go
    (publish-event-zerohits query)))

(defn zerohits-top100
  []
  (when-not (str/blank? redis-uri)
    (->>
      (car/zrevrangebyscore key-stats-event-zerohits :+inf :-inf :withscores :limit 0 100)
      (wcar*))))
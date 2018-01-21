(ns ordnerd.dictionary.swedish
  (:require [clojure.java.io :as io]
            [cheshire.core :refer :all]))

(def words
  (-> "dictionary_swedish.json"
      (io/resource)
      (slurp)
      (parse-string true)))

(defn- is-that-word?
  [query word]
  (let
    [q          (-> query (or "") (.toLowerCase))
     form       (get-in word [:form] "")
     pure-form  (.replaceAll form "~" "")
     inflection (get-in word [:inflection] "")]
    (if (or (.equalsIgnoreCase form q)
            ;(.contains inflection q)
            )
      word)))

(defn find-words
  [query]
  (let
    [check (fn [word] (is-that-word? query word))]
    (->>
      words
      (filter check)
      (into []))))
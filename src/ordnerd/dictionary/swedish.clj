(ns ordnerd.dictionary.swedish
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :refer :all]))

(def words
  (let [lines (-> "dictionary_swedish.json"
                  (io/resource)
                  (slurp)
                  (str/split-lines))]
    (->>
      lines
      (map #(parse-string % true)))))

(defn- is-inflection-contains?
  "Check if word inflection forms contains query string ignoring case."
  [word query]
  (let
    [q (->
         query
         (or "")
         (.toLowerCase))
     inflections (get word :inflections)]
    (some #{q} inflections)))

(defn search
  "Returns all words matching query string."
  [query]
    (->>
      words
      (filter #(is-inflection-contains? % query))
      (into [])))
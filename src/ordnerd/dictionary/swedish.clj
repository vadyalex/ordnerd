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

;(def load-words
;  (let [lines (-> "dictionary_swedish.json"
;                  (io/resource)
;                  (slurp)
;                  (str/split-lines))
;        is-not-blank? (fn [s] (not (str/blank? s)))
;        add-inflections #(let
;                           [word %
;                            form (->
;                                   (get word :form "")
;                                   (.toLowerCase))
;                            inflection (->
;                                         (get word :inflection "")
;                                         (.toLowerCase))
;                            inflections (->>
;                                          (str/split inflection #" ")
;                                          (filter is-not-blank?)
;                                          (into []))
;                            create-compound-inflections (fn [l] (let
;                                                                  [form-parts (str/split form #"~")]
;                                                                  (->>
;                                                                    l
;                                                                    (map (fn [i] (.replaceAll i "-" "")))
;                                                                    (map (fn [i] (str (first form-parts) i)))
;                                                                    (into []))))]
;                           (if (.contains form "~")
;                             (assoc word :inflections (cons (.replaceAll form "~" "") (create-compound-inflections inflections) ))
;                             (assoc word :inflections (cons form inflections))))]
;    (->>
;      lines
;      (map #(parse-string % true))
;      (map add-inflections))))
;
;
;(->>
;  load-words
;  (map generate-string)
;  (str/join \newline)
;  (spit "new-dict.json"))
(ns ordnerd.dictionary.swedish
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :refer :all]
            [clj-fuzzy.metrics :refer [dice]]))

(def words
  (let [lines (some-> "dictionary_swedish.jsonl"
                      (io/resource)
                      (slurp :encoding "utf-8")
                      (str/split-lines))]
    (->> lines
         (map #(parse-string % true)))))

(defn inflections-match?
  "Check if word inflection forms contains query string ignoring case."
  [word query]
  (let [inflections (:inflections word)]
    (->> inflections
         (filter some?)
         (map #(.replaceAll % "!" ""))
         (filter #(.equalsIgnoreCase % query))
         (first)
         (some?))))

(defn exact-search
  "Perform exact word search"
  [query]
  (->> words
       (filter #(inflections-match? % query))
       (into [])
       (not-empty)))

(defn fuzzy-search
  "Perform fuzzy search"
  [query]
  (let [min-score 0.5
        get-score (fn [form] (dice form query))]
        (->> words
            (filter (fn [word] (>= (get-score (:form word)) min-score)))
            (take 5)
            (into [])
            (not-empty))))

(defn search
  "Returns all words matching query string."
  [query & [fuzzy?]]
  (if-let [exact (exact-search query)]
    exact
    (if fuzzy? (fuzzy-search query))))

(defn random
  "Returns a random word from the dictionary."
  []
  (rand-nth words))

;(def load-words
;  (let [lines (-> "dictionary_swedish.jsonl"
;                  (io/resource)
;                  (slurp :encoding "utf-8")
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
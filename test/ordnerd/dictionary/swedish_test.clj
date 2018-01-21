(ns ordnerd.dictionary.swedish-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [ordnerd.dictionary.swedish :refer :all]))

(deftest test-dictionary-swedish

  (testing "find word: ab"
    (let [query "ab"
          result  (find-words query)]
      (is (= 1 (count result)))
      (is (= (get-in result [0 :form]) "AB"))
      (is (= (get-in result [0 :lexeme :usage]) "i firmanamn"))))

  (testing "find word: ord"
    (let [query "ord"
          result  (find-words query)]
      (is (= 1 (count result)))
      (pprint result)
      (is (= (get-in result [0 :form]) "ord"))
      (is (= (get-in result [0 :lexeme 0 :definition]) "minsta självständige språkliga enhet"))))
  )

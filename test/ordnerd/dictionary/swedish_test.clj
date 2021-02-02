(ns ordnerd.dictionary.swedish-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [ordnerd.dictionary.swedish :refer :all]))

(deftest test-dictionary-swedish

  (testing "find word 'ab'"
    (let [query "ab"
          result (search query)]
      ;(pprint result)
      (is (= 1 (count result)))
      (is (= (get-in result [0 :form]) "AB"))
      (is (= (get-in result [0 :lexeme 0 :usage]) "i firmanamn"))))

  (testing "find word 'ord'"
    (let [query "ord"
          result (search query)]
      ;(pprint result)
      (is (= 1 (count result)))
      (is (= (get-in result [0 :form]) "ord"))
      (is (= (get-in result [0 :lexeme 0 :definition]) "minsta självständige språkliga enhet"))))

  (testing "find word 'ord' using one of its forms 'ordet'"
    (let [query "ordet"
          result (search query)]
      ;(pprint result)
      (is (= 1 (count result)))
      (is (= (get-in result [0 :form]) "ord"))))

  (testing "find word 'binde~ord' by 'bindeordet'"
    (let [query "bindeordet"
          result (search query)]
      ;(pprint result)
      (is (= 1 (count result)))
      (is (= (get-in result [0 :form]) "binde~ord"))
      (is (= (get-in result [0 :lexeme 0 :definition]) "konjunktion"))))

  (testing "test random word from dictionary"
    (let [result (random)]
      ;(pprint result)
      (is (some? result))))

  (testing "find word 'sitt'"
    (let [query "sitt"
          result (search query)]
      ;(pprint result)
      (is (= 3 (count result)))))

  (testing "find word 'ger sig'"
    (let [query "ger sig"
          result (search query)]
      (is (= 1 (count result)))))

  (testing "find word 'ger sig'"
    (let [query "ge sig"
          result (search query)]
      (is (= 0 (count result)))))

  (testing "find word 'ger sig'"
    (let [query "ge sig"
          result (search query :fuzzy)]
      (println "\"ge sig\" -> " )
      (pprint result)
      (is (= 5 (count result))))))  

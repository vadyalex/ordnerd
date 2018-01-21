(ns ordnerd.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [ordnerd.handler :refer :all]))

(deftest test-app
  (testing "get word: ab"
    (let [response (app (mock/request :get "/dictionary/swedish/ab"))]
      (is (= (:status response) 200))))

  (testing "get word: egz"
    (let [response (app (mock/request :get "/dictionary/swedish/egz"))]
      (is (= (:status response) 204))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

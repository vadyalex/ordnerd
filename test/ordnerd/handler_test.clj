(ns ordnerd.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [ordnerd.handler :refer :all]))

(deftest test-app

  (testing "/"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "OK"))))

  (testing "get word: ab"
    (let [response (app (mock/request :get "/dictionary/swedish/ab"))]
      (is (= (:status response) 200))))

  (testing "get word: egz"
    (let [response (app (mock/request :get "/dictionary/swedish/egz"))]
      (is (= (:status response) 204))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404))))

  (testing "Telegram webhook update"
    (let [request (-> (mock/request :post "/bot/telegram/WEBHOOK")
                      (mock/json-body {:message {:message-id "123" :text "/ord ord"}}))
          response (app request)]
      (is (= (:status response) 202))))

  (testing "Telegram webhook - empty update"
    (let [request (-> (mock/request :post "/bot/telegram/WEBHOOK")
                      (assoc :body ""))
          response (app request)]
      (is (= (:status response) 202))))
  )

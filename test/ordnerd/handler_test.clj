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

  (testing "get word: ordet"
    (let [response (app (mock/request :get "/dictionary/swedish/ordet"))]
      (is (= (:status response) 200))))

  (testing "get word: egz"
    (let [response (app (mock/request :get "/dictionary/swedish/egz"))]
      (is (= (:status response) 204))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404))))


  (with-redefs-fn

    {#'ordnerd.handler/telegram-send-message (fn [chat-id text] (println "SENDING TO TELEGRAM:" \newline text))}
    ;{#'clj-http.client/get (fn [url & [req]] (println "SENDING TO TELEGRAM:" url) {:status 200})}

    #(do

       (testing "Telegram webhook - update does not contain chat-id"
         (let [request (-> (mock/request :post "/bot/telegram/WEBHOOK")
                           (mock/json-body {:message {:message-id "123" :text "/ord ord"}}))
               response (app request)]
           (Thread/sleep 2000)
           (is (= (:status response) 202))))

       (testing "Telegram webhook - empty update"
         (let [request (-> (mock/request :post "/bot/telegram/WEBHOOK")
                           (assoc :body ""))
               response (app request)]
           (Thread/sleep 2000)
           (is (= (:status response) 400))))

       (testing "Telegram webhook - unknown word"
         (let [request (-> (mock/request :post "/bot/telegram/WEBHOOK")
                           (mock/json-body {"update_id" 123
                                            "message"
                                                        {"message_id" 3
                                                         "from"
                                                                      {"id"            986413
                                                                       "is_bot"        false
                                                                       "first_name"    "Bond"
                                                                       "username"      "jamesbond"
                                                                       "language_code" "en-US"}

                                                         "chat"
                                                                      {"id"         373734652
                                                                       "first_name" "Bond"
                                                                       "username"   "jamesbond"
                                                                       "type"       "private"}
                                                         "date"       1516570408
                                                         "text"       "egz"}}))
               response (app request)]
           (Thread/sleep 2000)
           (is (= (:status response) 202))))

       (testing "Telegram webhook - sitt"
         (let [request (-> (mock/request :post "/bot/telegram/WEBHOOK")
                           (mock/json-body {
                                            "update_id" 123
                                            "message"
                                                        {
                                                         "message_id" 3
                                                         "from"       {
                                                                       "id"            986413
                                                                       "is_bot"        false
                                                                       "first_name"    "Bond"
                                                                       "username"      "jamesbond"
                                                                       "language_code" "en-US"
                                                                       }

                                                         "chat"       {
                                                                       "id"         373734652
                                                                       "first_name" "Bond"
                                                                       "username"   "jamesbond"
                                                                       "type"       "private"
                                                                       }
                                                         "date"       1516570408
                                                         "text"       "sitt"
                                                         }
                                            }))
               response (app request)]
           (Thread/sleep 2000)
           (is (= (:status response) 202))))

       (testing "Telegram webhook - /start"
         (let [request (-> (mock/request :post "/bot/telegram/WEBHOOK")
                           (mock/json-body {
                                            "update_id" 494609584,
                                            "message"   {
                                                         "message_id" 1234
                                                         "from"       {
                                                                       "id"            986413
                                                                       "is_bot"        false
                                                                       "first_name"    "Bond"
                                                                       "username"      "jamesbond"
                                                                       "language_code" "en-US"
                                                                       }
                                                         "chat"       {
                                                                       "id"         986413
                                                                       "first_name" "Bond"
                                                                       "username"   "jamesbond"
                                                                       "type"       "private"
                                                                       }
                                                         "date"       1517082121
                                                         "text"       "/start"
                                                         "entities"   [
                                                                       {
                                                                        "offset" 0
                                                                        "length" 6
                                                                        "type"   "bot_command"
                                                                        }
                                                                       ]
                                                         }
                                            }))
               response (app request)]
           (Thread/sleep 2000)
           (is (= (:status response) 202))))

       (testing "Telegram webhook - /slumpa"
         (let [request (-> (mock/request :post "/bot/telegram/WEBHOOK")
                           (mock/json-body {
                                            "update_id" 494609584,
                                            "message"   {
                                                         "message_id" 1235
                                                         "from"       {
                                                                       "id"            986413
                                                                       "is_bot"        false
                                                                       "first_name"    "Bond"
                                                                       "username"      "jamesbond"
                                                                       "language_code" "en-US"
                                                                       }
                                                         "chat"       {
                                                                       "id"         986413
                                                                       "first_name" "Bond"
                                                                       "username"   "jamesbond"
                                                                       "type"       "private"
                                                                       }
                                                         "date"       1517082121
                                                         "text"       "/slumpa"
                                                         "entities"   [
                                                                       {
                                                                        "offset" 0
                                                                        "length" 7
                                                                        "type"   "bot_command"}
                                                                       ]
                                                         }
                                            }))
               response (app request)]
           (Thread/sleep 2000)
           (is (= (:status response) 202))))

       )
    )
  )

(defproject ordnerd "1.0.0-SNAPSHOT"

  :description "Swedish dictionary service"

  :url "https://github.com/vadyalex/ordnerd"

  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.4.474"]
                 [compojure "1.5.0"]
                 [cheshire "5.8.0"]
                 [ring-server "0.4.0"]
                 [ring/ring-defaults "0.2.0"]
                 [ring/ring-mock "0.3.2"]
                 [http-kit "2.2.0"]
                 [clj-http "3.1.0"]
                 [environ "1.1.0"]
                 [ch.qos.logback/logback-classic "1.1.7"]]

  :uberjar-name "ordnerd.jar"
  :main ordnerd.core

  :profiles {:uberjar {:aot :all}})

(defproject ordnerd "1.0.0-SNAPSHOT"

  :description "Swedish dictionary service"

  :url "https://github.com/vadyalex/ordnerd"

  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.10.2"]
                 [org.clojure/data.json "1.0.0"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.clojure/core.async "1.3.610"]
                 [compojure "1.6.2"]
                 [cheshire "5.10.0"]
                 [ring-server "0.5.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-mock "0.4.0"]
                 [http-kit "2.5.1"]
                 [clj-http "3.12.1"]
                 [environ "1.2.0"]
                 [ch.qos.logback/logback-classic "1.3.0-alpha5"]
                 ;[com.taoensso/carmine "2.18.0"]
                 [clj-fuzzy "0.4.1"]]

  :uberjar-name "ordnerd.jar"
  :main ordnerd.core

  :profiles {:uberjar {:aot :all}})

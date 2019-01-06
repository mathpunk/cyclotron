(defproject cyclotron "0.6.1-SNAPSHOT"
  :description "LogicGate E2E/CI reports"
  :url ""
  :license {:name "its ours u can't have it"
            :url ""}
  :dependencies [[org.clojure/clojure "1.10.0-RC5"]
                 [org.clojure/tools.cli "0.4.1"]
                 [metasoarous/oz "1.3.1"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/data.xml "0.0.8"]
                 [clj-jgit "0.8.10"]
                 [com.cognitect/transcriptor "0.1.5"]]
  :main ^:skip-aot cyclotron.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

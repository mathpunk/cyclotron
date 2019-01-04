(defproject cyclotron "0.5.0-SNAPSHOT"
  :description "LogicGate CI tool"
  :url ""
  :license {:name "its ours u can't have it"
            :url ""}
  :dependencies [
                 [org.clojure/clojure "1.10.0-RC3"]
                 [metasoarous/oz "1.3.1"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/data.xml "0.0.8"]
                 [datawalk "0.1.12"]
                 [tolitius/xml-in "0.1.0"]
                 [clj-jgit "0.8.10"]
                 [org.clojure/data.zip "0.1.2"]
                 [com.cognitect/transcriptor "0.1.5"]
                 ]
  :main ^:skip-aot cyclotron.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

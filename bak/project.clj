(defproject cyclotron "0.3.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [tolitius/xml-in "0.1.0"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [clojure.java-time "0.3.2"]
                 ;; [datawalk "0.1.12"]
                 ;; [clj-jgit "0.8.10"]
                 ;; [me.raynes/conch "0.8.0"]
                 ;; dev
                 [com.cognitect/transcriptor "0.1.5"]]
  :main ^:skip-aot cyclotron.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

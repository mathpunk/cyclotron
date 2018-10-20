(defproject cyclotron "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [tolitius/xml-in "0.1.0"]
                 [clj-gitlab "0.1.0"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]
                 [code-maat "1.0.1"]
                 [org.clojure/data.xml "0.0.8"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]]
  :plugins [[lein-ring "0.12.4"]]
  :ring {:handler cyclotron.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})

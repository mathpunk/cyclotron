(ns cyclotron.utils
  (:require [clojure.xml :as xml]
            [clojure.java.io :as io]))

(defn str->int [s]
  (Integer. s))

(defn str->float [s]
  (Double. s))

(def sample-report (io/file "/home/man/projects/developing/cyclotron/resources/reports/sample.xml"))

(def mock-run {:cyclotron.run/report (xml-seq (xml/parse sample-report))})

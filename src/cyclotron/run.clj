(ns cyclotron.run
  (:import org.xml.sax.SAXParseException)
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [cyclotron.cache :as cache]
            [cyclotron.case :as case]
            [clojure.xml :as xml]
            [cyclotron.utils :refer [str->int str->float]]
            [clojure.set :as set]))


;; Metadata about the test run is stored in the path
;; ===================================================
(defn date
  [path]
  (second (re-find #"reports/(\d\d\d\d/\d\d/\d\d)/" path)))

(defn job
  "A name found in .gitlab-ci"
  [path]
  (second (re-find #"\d\d\d\d/\d\d/\d\d/([\w-]+)" path)))

(defn pipeline
  [path]
  (second (re-find #"pipeline-(\d+)" path)))

(defn suites
  "If known. Otherwise nil"
  [path]
  (if-let [suite-string (re-find #"suites-(\w+,?)+" path)]
    (-> suite-string
        first
        (string/split #"suites-")
        second
        (string/split #","))
    nil))

(defn revision [path]
  (second (re-find #"revision-(\w+)" path)))

(defn run-meta [path]
  {::date (date path)
   ::pipeline (pipeline path)
   ::revision (revision path)
   ::suites (suites path)
   ::job (job path)})

(defn run-data
  "Parses a file into an xml-seq"
  [file]
  (xml-seq (xml/parse file)))

(defn- log-xml-malformation [error meta]
  ;; TODO: This would be interesting https://github.com/clojure/tools.logging
  ;; NOTE: This was useful for helping me understand that my xml failures are pretty
  ;; deep in our run history, but, it's kind of obnoxious. What's the "real" logging
  ;; solution?
  #_(println (str error " in pipeline " (::pipeline meta) " on " (::date meta))))

(defn create-run [file]
  (let [path (.getPath file)
        meta (run-meta path)
        report (try (run-data file)
                    (catch SAXParseException e
                      (let [error (if (empty? (slurp file))
                                    :cyclotron.run.error/empty-junit-report
                                    :cyclotron.run.error/unknown-xml-error)]
                        (log-xml-malformation error meta))))]
    (merge meta {::report report
                 ::cases (case/breakdown report)})))

(def runs
  (->> cache/cache
       (map create-run)
       (sort-by :cyclotron.run/pipeline)
       reverse))

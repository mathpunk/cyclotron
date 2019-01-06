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

;; (s/fdef run-data
;;   :args (s/cat :xml-seq seq?))

(defn- log-xml-malformation [error meta]
  ;; TODO: This would be interesting https://github.com/clojure/tools.logging
  ;; NOTE: This was useful for helping me understand that my xml failures are pretty
  ;; deep in our run history, but, it's kind of obnoxious. What's the "real" logging
  ;; solution?
  #_(println (str error " in pipeline " (::pipeline meta) " on " (::date meta))))

(alias 'count (create-ns 'cyclotron.cases.count))
(alias 'stats (create-ns 'cyclotron.run.stats))

(defn create-run [file]
  (let [path (.getPath file)
        meta (run-meta path)
        report (try (run-data file)
                    (catch SAXParseException e
                      (let [error (if (empty? (slurp file))
                                    :cyclotron.run.error/empty-junit-report
                                    :cyclotron.run.error/unknown-xml-error)]
                        (log-xml-malformation error meta))))
        cases (case/breakdown report)
        passed (count (:passed cases))
        failed (count (:failed cases))
        skipped (count (:skipped cases))
        attempted (+ passed failed)
        success-ratio (if (zero? attempted)
                        (float 0)
                        (float (/ passed attempted)))]
    (merge meta {::report report
                 ::cases cases
                 ::count/passed passed
                 ::count/failed failed
                 ::count/skipped skipped
                 ::count/attempted attempted
                 ::stats/success-ratio success-ratio})))

(def runs
  (->> cache/cache
       (map create-run)
       (sort-by :cyclotron.run/pipeline)
       reverse))

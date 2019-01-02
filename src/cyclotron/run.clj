(ns cyclotron.run
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [cyclotron.cache :as cache]
            [clojure.data.xml :as xml]
            [cyclotron.utils :refer [str->int str->float]]
            [clojure.set :as set]))


;; Metadata about the test run is stored in the path
;; ===================================================
(defn date [path]
  (second (re-find #"reports/(\d\d\d\d/\d\d/\d\d)/" path)))

(defn job [path]
  (second (re-find #"\d\d\d\d/\d\d/\d\d/([\w-]+)" path)))

(defn pipeline [path]
  (second (re-find #"pipeline-(\d+)" path)))

(defn suites
  "If known, otherwise nil"
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



;; The run data with some stats parsed
;; =========================================
(defn run-data [file]
  (xml/parse-str (slurp file)))

(defn run-stats [file]
  (let [xml (run-data file)]
    (-> (:attrs xml)
        (update :disabled str->int)
        (update :errors str->int)
        (update :failures str->int)
        (update :tests str->int)
        (update :time str->float)
        (set/rename-keys {:disabled :cyclotron.run.count/disabled
                          :errors   :cyclotron.run.count/errors
                          :failures :cyclotron.run.count/failures
                          :tests    :cyclotron.run.count/specs
                          :time     :cyclotron.run/elapsed-time}))))


;; The finished run map
;; ======================================
(defn create-run [file]
  (let [path (.getPath file)]
    (merge (run-meta path)
           {::data (run-data file)}
           (run-stats file))))


;; The collection of test runs to date
;; =========================================
(def runs
  (->> cache/cache
       (map create-run)
       (sort-by :cyclotron.run/pipeline)
       reverse))


;; Other helpful things
;; =================================
(defn successful? [run]
  (= 0 (:cyclotron.run.count/failures run)))


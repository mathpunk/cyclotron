(ns cyclotron.reports
  (:refer-clojure :exclude [load read])
  (:require [clojure.java.shell :as sh]
            [clojure.string :as string]
            [clojure.data.xml :as data]
            [java-time :as time]
            [clojure.java.io :as io]))

(def bucket "s3://gitlab-logicgate-artifacts")

(def reports-cache (io/as-file (io/resource "reports")))

(defn- cp [target]
  (apply sh/sh ["aws" "--profile" "lg" "s3" "cp" "--recursive" bucket target]))

(defn load []
  (let [results (cp (.getPath reports-cache))]
    (if (empty? (:err results))
      ::ok
      {::error (:err results)})))

(comment
  (load))

(def cached
  (->> reports-cache
       file-seq
       (filter #(.isFile %))))

(defn stage? [file]
  (re-find #"stage-e2e" (.getPath file)))

(def stage
  (filter stage? cached))

(defn fresh? [file]
  (let [path (.getPath file)
        date (time/format "yyyy/MM/dd" (time/local-date))]
    (re-find (re-pattern date) path)))

(def nightly-stage
  (->> cached
       (filter stage?)
       (filter fresh?)))

(defn stable? [file]
  (re-find #"test-e2e" (.getPath file)))

(def nightly-test
  (->> cached
       (filter stable?)
       (filter fresh?)))

(defn run-date [report-file]
  (re-find #"\d\d\d\d/\d\d/\d\d" (.getPath report-file)))

(defn job-name [report-file]
  (let [path (.getPath report-file)
        date (run-date report-file)]
    (string/join (take-while #(not= \/ %) (drop 11 (drop (string/index-of path date) path))))))

(defn pipeline-id [report-file]
  (let [path (.getPath report-file)]
    (second (re-find #"pipeline-(\d+)" path))))

(defn revision-sha [report-file]
  (let [path (.getPath report-file)]
    (second (re-find #"revision-(\w+)" path))))

(defn create-report [report-file]
  {:cyclotron.run/date (run-date report-file)
   :cyclotron.run/pipeline (pipeline-id report-file)
   :cyclotron.run/revision (revision-sha report-file)
   :cyclotron.run/job (job-name report-file)
   :cyclotron.run/data (data/parse-str (slurp report-file))
   })

(comment "Sanity check"
  (set! *print-length* 10)

  (set! *print-level* 2)

  nightly-test

  (def report-file (first nightly-test))

  (.getPath report-file)
  ;; what you'd expect

  (pipeline-id report-file)
  ;; what you'd expect

  (revision-sha report-file)
  ;; what you'd expect
  )

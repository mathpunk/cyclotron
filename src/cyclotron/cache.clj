(ns cyclotron.cache
  (:refer-clojure :exclude [load update])
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.string :as string]))

(def cache-location (io/as-file (io/resource "reports")))


(defn load
  ([date]
   (let [bucket "s3://gitlab-logicgate-artifacts/"
         target (.getPath cache-location )]
     (apply sh/sh ["aws" "--profile" "lg" "s3" "cp" "--recursive" (str bucket date) target])))
  ([]
   (load nil)))


(defn init []
  (let [results (load)]
    (if (empty? (:err results))
      ::ok
      {::error (:err results)})))


(def cache
  (->> cache-location
       file-seq
       (filter #(.isFile %))))

(comment

  (def fs (file-seq cache-location))

  (set! *print-length* 10)

  (set! *print-level* 5)

  (count fs)

  (require '[clojure.pprint :refer [pprint]])

  (pprint (take 10 (reverse fs)))

  )

(comment

  (declare update) ;; A nice-to-have, maybe, but right now it only takes a minute or so to init.

  (defn today [] (.format (java.text.SimpleDateFormat. "yyyy/MM/dd") (new java.util.Date)))

  (today)

  (load (today))

  


  (def success-msg "Completed 27.8 KiB/237.6 KiB (28.3 KiB/s) with 4 file(s) remaining\rdownload: s3://gitlab-logicgate-artifacts/2018/12/06/stage-e2e/pipeline-14339-revision-bb17522ccf62d00daf46ebb51a33248ae0b73f40/junitresults.xml to resources/reports/stage-e2e/pipeline-14339-revision-bb17522ccf62d00daf46ebb51a33248ae0b73f40/junitresults.xml\nCompleted 27.8 KiB/237.6 KiB (28.3 KiB/s) with 3 file(s) remaining\rCompleted 46.1 KiB/237.6 KiB (45.9 KiB/s) with 3 file(s) remaining\rdownload: s3://gitlab-logicgate-artifacts/2018/12/06/stage-e2e/pipeline-14341-revision-bb17522ccf62d00daf46ebb51a33248ae0b73f40/junitresults.xml to resources/reports/stage-e2e/pipeline-14341-revision-bb17522ccf62d00daf46ebb51a33248ae0b73f40/junitresults.xml\nCompleted 46.1 KiB/237.6 KiB (45.9 KiB/s) with 2 file(s) remaining\rCompleted 85.7 KiB/237.6 KiB (81.3 KiB/s) with 2 file(s) remaining\rdownload: s3://gitlab-logicgate-artifacts/2018/12/06/test-e2e/pipeline-14335-revision-bb17522ccf62d00daf46ebb51a33248ae0b73f40/junitresults.xml to resources/reports/test-e2e/pipeline-14335-revision-bb17522ccf62d00daf46ebb51a33248ae0b73f40/junitresults.xml\nCompleted 85.7 KiB/237.6 KiB (81.3 KiB/s) with 1 file(s) remaining\rCompleted 237.6 KiB/237.6 KiB (189.2 KiB/s) with 1 file(s) remaining\rdownload: s3://gitlab-logicgate-artifacts/2018/12/06/stage-e2e/pipeline-14338-revision-bb17522ccf62d00daf46ebb51a33248ae0b73f40/junitresults.xml to resources/reports/stage-e2e/pipeline-14338-revision-bb17522ccf62d00daf46ebb51a33248ae0b73f40/junitresults.xml\n"

    )

  (require '[clojure.string :as string])

  (set! *print-length* nil)


  (filter #(re-find #"^Completed" %) (string/split success-msg #"\r|\n")))

(comment
  (take 5 cache)

  (load (today))

  (init)

  )


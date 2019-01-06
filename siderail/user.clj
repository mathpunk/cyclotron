(ns user
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as test]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.pprint :refer [print-table pprint]]
            [cognitect.transcriptor :refer [check!]]))

(do "Tools"

    (defn set-print
      [{:keys [level length]}]
      (do (set! *print-length* length)
          (set! *print-level* level)))

    (defn purge!
      "Unbinds all public vars. For use when you're done messing with one concept in userspace."
      []
      (map #(ns-unmap 'user %) (keys (ns-publics 'user))))


    (set-print {:length 8 :level 3}))

(comment

  "Possibly load the cache (in it's entirety, sigh)"
  (do
    (require '[cyclotron.cache :as cache])
    (cache/init)))

(do "Require your code"
    (require '[cyclotron.run :as run])
    (require '[cyclotron.report :as report])
    (require '[cyclotron.case :as case]))


(do "Don't break reports"

    (require '[cyclotron.report :as report])

    (report/ascii-summary-recent 10)

    (report/ascii-summary-successes 10)

    )


(do

  "14899 was 96% successful, and then we tank."

  (def ffilter (comp first filter))

  (alias 'run 'cyclotron.run)

  (defn pipeline [id]
    (ffilter #(= (str id) (::run/pipeline %)) run/runs))

  (def successful (pipeline 14899))

  (def kaput (last (take-while #(not= "14899" (:cyclotron.run/pipeline %)) run/runs)))

  (pprint (take 5 (drop 50 run/runs)))

  (defn next-pipeline [id]
    (last (take-while #(not= (str id) (:cyclotron.run/pipeline %)) run/runs)))

  "What are the differences?"

  (require '[cyclotron.code :as code])

  (apply code/changed-files (map ::run/revision [successful kaput]))

  (set-print {:length 10 :level 5})

  (keys kaput)

  (keys (report/summary kaput))

  (report/summary successful)

  (defn successful? [rate run]
    (> (report/summary run) rate))

  (::run/pipeline (ffilter #(successful 0.95 %) run/runs))

  (:cyclotron.run.stats/success-ratio (ffilter #(successful 1.0 %) run/runs))

  (::run/pipeline (ffilter #(successful 0.90 %) run/runs))

  (:cyclotron.run.stats/success-ratio (report/summary kaput))

  (:cyclotron.run.stats/success-ratio (report/summary successful))

  )


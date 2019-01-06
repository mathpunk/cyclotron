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

(comment "Load the cache (in its entirety)"
         (do
           (require '[cyclotron.cache :as cache])
           (cache/init)))

(do "Simple Reports"

    (require '[cyclotron.report :as report])

    (report/ascii-summary-recent 10)

    (report/ascii-summary-successes 10)

    )

(do "1. How many perfect, good, middlin', poor, and zero runs have we had?"

    (require '[cyclotron.run :as run])

    (defn bucket [ratio]
      (cond
        (= ratio 1.0)  :perfect
        (>= ratio 0.9) :good
        (>= ratio 0.5) :middlin
        (> ratio 0.0 ) :poor
        (= ratio 0.0)  :zero
        :default       ratio))

    (frequencies (map (fn [run] (bucket (:cyclotron.run.stats/success-ratio run))) run/runs)))

(do "2. What was the last perfect (and non-trivial) pipeline? When was it? What suites were run? How many tests?"

    (alias 'run 'cyclotron.run)
    (alias 'count (create-ns 'cyclotron.cases.count))

    (defn non-trivial? [run]
      (> (::count/attempted run) 0))

    (defn no-failures? [run]
      (zero? (::count/failed run)))

    (defn flying-colors? [run]
      (and (non-trivial? run) (no-failures? run)))

    (def ffilter (comp first filter))

    (select-keys (ffilter flying-colors? run/runs) [::run/pipeline ::run/date ::run/suites ::count/attempted]))

(do "3. What was the next run like? What was its success ratio?"

    (defn next-pipeline [id]
      (last (take-while #(not= (str id) (:cyclotron.run/pipeline %)) run/runs)))

    (select-keys (next-pipeline 14898) [::run/pipeline ::run/date ::run/suites ::count/attempted :cyclotron.run.stats/success-ratio]))

(do "4. What files have changed since that perfect run?"

    (require '[cyclotron.code :as code])

    (set-print {:length 50 :level 3})

    (let [flying-colors-run (ffilter flying-colors? run/runs)]
      (pprint (take 50 (code/changed-files (::run/revision flying-colors-run))))))

(do "5. What changed between the perfect run and the next run?"

    (let [flying-colors-run (ffilter flying-colors? run/runs)
          following-run (next-pipeline (::run/pipeline flying-colors-run))]
      (code/changed-files (::run/revision flying-colors-run) (::run/revision following-run))))



(comment "FUTURE: Filtering by success rate"

         ;; (require '[cyclotron.report :as report])

         ;; (defn successful? [rate run]
         ;;   (> (report/summary run) rate))

         ;; (::run/pipeline (ffilter #(successful 0.95 %) run/runs))

         )


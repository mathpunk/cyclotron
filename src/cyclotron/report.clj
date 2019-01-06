(ns cyclotron.report
  (:require [clojure.string :as string]
            [cyclotron.case :as case]
            [clojure.pprint :refer [print-table]]
            [clojure.set :as set]))

(alias 'case 'cyclotron.case)
(alias 'count (create-ns 'cyclotron.cases.count))
(alias 'run 'cyclotron.run)
(alias 'stats (create-ns 'cyclotron.run.stats))

(defn labeled-summary [run]
  (->  run
       (update ::stats/success-ratio #(Math/round (* 100 %)))
       (set/rename-keys {::run/date "Date"
                         ::run/pipeline "Pipeline"
                         ::run/job "Job"
                         ::run/suites "Suites"
                         ::run/revision "Revision SHA"
                         ::count/attempted "Total"
                         ::stats/success-ratio "%"})))

(defn ascii-summary-successes [n]
  (->> run/runs
       (map labeled-summary)
       (remove #(zero? (::count/passed %)))
       (take n)
       (print-table ["Date" "%" "Total" "Job" "Suites" "Pipeline" "Revision SHA"])))

(defn ascii-summary-recent [n]
  (->> run/runs
       (map labeled-summary)
       (take n)
       (print-table ["Date" "%" "Total" "Job" "Suites" "Pipeline" "Revision SHA"])))

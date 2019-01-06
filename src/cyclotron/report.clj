(ns cyclotron.report
  (:require [clojure.string :as string]
            [cyclotron.case :as case]
            [clojure.pprint :refer [print-table]]
            [clojure.set :as set]))

(alias 'case 'cyclotron.case)

(alias 'run 'cyclotron.run)

(defn attempted [{:keys [cyclotron.case/passed cyclotron.case/failed]}]
  (+ passed failed))

(alias 'stats (create-ns 'cyclotron.run.stats))

(defn success-rate [{:keys [cyclotron.case/passed cyclotron.case/failed] :as counts}]
  (let [total (attempted counts)]
    (if (zero? total)
      (float 0)
      (float (/ passed (attempted counts ))))))


(defn summary [run]
  (let [score (case/score (:cyclotron.run/report run))
        stats (merge score {::stats/success-ratio (success-rate score)
                            ::stats/attempted (attempted score)})]
    (-> run
        (merge stats)
        (update ::run/suites #(if (nil? %) "<unavailable>" (string/join ", " %)))
        (update ::stats/success-ratio #(Math/round (* 100 %)))
        (set/rename-keys {::run/date "Date"
                          ::run/pipeline "Pipeline"
                          ::run/job "Job"
                          ::run/suites "Suites"
                          ::case/failed "Failed"
                          ::case/skipped "Skipped"
                          ::run/revision "Revision SHA"
                          ::stats/attempted "Total"
                          ::stats/success-ratio "%"}))))

(defn ascii-summary-successes [n]
  (->> run/runs
       (map summary)
       (remove #(zero? (::case/passed %)))
       (take n)
       (print-table ["Date" "%" "Total" "Job" "Suites" "Pipeline" "Revision SHA"])))

(defn ascii-summary-recent [n]
  (->> run/runs
       (map summary)
       (take n)
       (print-table ["Date" "%" "Total" "Job" "Suites" "Pipeline" "Revision SHA"])))

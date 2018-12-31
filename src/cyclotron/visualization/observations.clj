(ns cyclotron.visualization.observations)

(do
  (create-ns 'cyclotron.run.count)
  (alias 'stats 'cyclotron.run.count)
  (alias 'run 'cyclotron.run))

(defn successes [meas]
  (max 0 (- (::stats/specs meas)
            (+ (::stats/errors meas)
               (::stats/failures meas)
               (::stats/disabled meas)))))

(defn measurements [run]
  (let [meas (run/measure run)]
    (-> (assoc meas ::stats/successes (successes meas))
        (select-keys [::run/date
                      ::run/pipeline
                      ::stats/specs
                      ::stats/errors
                      ::stats/disabled
                      ::stats/failures
                      ::stats/successes
                      ::run/elapsed-time]))))

(defn observe-key [k run]
  {:pipeline (::run/pipeline run)
   :count (get run k)
   :result k})

(defn observations [runs]
  (for [r (map measurements runs)
        k [:cyclotron.run.count/failures
           :cyclotron.run.count/successes
           :cyclotron.run.count/errors
           :cyclotron.run.count/disabled]]
    (observe-key k r)))


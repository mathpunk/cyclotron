(ns user)

(do

  (require '[clojure.spec.alpha :as s]
           '[clojure.spec.test.alpha :as t]
           '[clojure.spec.gen.alpha :as g]
           '[clojure.string :as string]
           '[clojure.java.io :as io]
           '[clojure.pprint :refer [print-table pprint]]
           '[cognitect.transcriptor :refer [check!]])

  (set! *print-length* 10)

  (set! *print-level* 4))


(require '[oz.core :as oz])

(oz/start-plot-server!)

(ns cyclotron.viz)

(require '[cyclotron.run :as run])

(defn group-data [& names]
  (apply concat (for [n names]
                  (map-indexed (fn [i x] {:x i :y x :col n}) (take 8 (repeatedly #(rand-int 100)))))))

(alias 'run 'cyclotron.run)

(group-data "bear" "dog" "lion" "witch")



(do



  (defn measurements [run]
    (select-keys (run/measure run ) [:cyclotron.run/date
                                     :cyclotron.run/pipeline
                                     :cyclotron.run.count/specs
                                     :cyclotron.run.count/errors
                                     :cyclotron.run.count/disabled
                                     :cyclotron.run.count/failures
                                     :cyclotron.run/elapsed-time]))

  (run/measure (first run/runs))

  (measurements (first run/runs))

  (defn observe-key [k run]
    {:x (::run/pipeline run)
     :y (get run k)
     :col k})

  (set! *print-length* 20)

  (def run-counts 
    (for [r (map measurements run/runs)
          k [:cyclotron.run.count/failures :cyclotron.run.count/specs
             :cyclotron.run.count/disabled :cyclotron.run.count/errors]]
      (take 20 (observe-key k r))))

  (defn projection [runs]
    runs
    #_(filter #(= "test-e2e" (:cyclotron.run/job %)) runs))

  (def possible-runs
    (take 40 (map measurements (projection run/runs ))))

  (def values
    (for [r possible-runs
          k [:cyclotron.run.count/failures]]
      (observe-key k r)))

  (def stacked-bar
    {:data {:values values}
     :mark "bar"
     :encoding {:x {:field "x"
                    :type "ordinal"}
                :y {:aggregate "sum"
                    :field "y"
                    :type "quantitative"}
                :color {:field "col"
                        :type "nominal"}}})

  (oz/v! stacked-bar))

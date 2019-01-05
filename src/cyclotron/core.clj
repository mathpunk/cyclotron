(ns cyclotron.core
  (:gen-class)
  (:require [cyclotron.cache :as cache]
            [cyclotron.run :as run]
            #_[cyclotron.visualization.components :refer [color]]
            [cyclotron.report :as report]
            #_[oz.core :as oz]))


(defn -main
  "Don't do much"
  [& args]
  (println "\nSUCCESSFUL")
  (report/ascii-summary-successes 10)
  (println "\nRECENT")
  (report/ascii-summary-recent 10))




;; (create-ns 'cyclotron.run.count)

;; (alias 'count 'cyclotron.run.count)

;; (alias 'run 'cyclotron.run)

;; (defn successes [meas]
;;   (max 0 (- (::count/specs meas)
;;             (+ (::count/errors meas)
;;                (::count/failures meas)
;;                (::count/disabled meas)))))

;; (defn measurements [run]
;;   (let [meas (run/measure run)]
;;     (-> (assoc meas ::stats/successes (successes meas))
;;         (select-keys [::run/date
;;                       ::run/pipeline
;;                       ::stats/specs
;;                       ::stats/errors
;;                       ::stats/disabled
;;                       ::stats/failures
;;                       ::stats/successes
;;                       ::run/elapsed-time]))))

;; (defn observe-key [k run]
;;   {:pipeline (::run/pipeline run)
;;    :count (get run k)
;;    :result k})

;; (defn observations [runs]
;;   (for [r (map measurements runs)
;;         k [:cyclotron.run.count/failures
;;            :cyclotron.run.count/successes
;;            :cyclotron.run.count/errors
;;            :cyclotron.run.count/disabled]]
;;     (observe-key k r)))


;; (create-ns 'cyclotron.run.count)

;; (alias 'run 'cyclotron.run)

;; (defn recent-runs-chart [runs]
;;   (let [values (obs/observations runs)]
;;     {:title "12/19"
;;      :data {:values values}
;;      :mark "bar"
;;      :encoding {:x {:field "pipeline"
;;                     :type "ordinal"}
;;                 :y {:aggregate "sum"
;;                     :field "count"
;;                     :type "quantitative"}
;;                 :color {:field "result"
;;                         :type "nominal"
;;                         :scale {:domain ["disabled" "errors" "failures" "successes"]
;;                                 :range [(color :disabled) (color :error) (color :failure) (color :success)]}}}}))

;; (defn date-partition [runs]
;;   (->> run/runs
;;        (remove #(nil? (::run/date %)))
;;        (partition-by ::run/date)))

;; (def partitioned-runs (date-partition run/runs))

;; (defn row-of-multiples [row]
;;   [:div {:style {:display "flex" :flex-direction "row"}}
;;    (map (fn [part]
;;           [:vega-lite (recent-runs-chart (nth partitioned-runs part))]) (range (* row 4) (* (+ 1 row ) 4)))])

;; (def chart-1 (dissoc (recent-runs-chart (take 50 run/runs)) :title))

;; (defn view! []
;;   (oz/view! [:div
;;              [:h1 "Daily Pipelines"]
;;              (row-of-multiples 0)
;;              (row-of-multiples 1)
;;              (row-of-multiples 2)]
;;             [:h3 "History"]
;;             [:vega-lite chart-1]))

;; (defn -main
;;   "Load and visualize test reports"
;;   [& args]
;;   (do (cache/init) 
;;       (oz/start-plot-server!) 
;;       (Thread/sleep 3000)
;;       (view!)

;;       ))

;; (comment

;;   #_(require '[clojure.spec.alpha :as s]
;;              '[clojure.spec.test.alpha :as t]
;;              '[clojure.spec.gen.alpha :as g]
;;              '[clojure.string :as string]
;;              '[clojure.java.io :as io]
;;              '[clojure.pprint :refer [print-table pprint]]
;;              '[cognitect.transcriptor :refer [check!]])
;;   )





;; (comment 

;;   (ns my.program
;;     (:require [clojure.tools.cli :refer [parse-opts]])
;;     (:gen-class))

;;   (def cli-options
;;     ;; An option with a required argument
;;     [["-p" "--port PORT" "Port number"
;;       :default 80
;;       :parse-fn #(Integer/parseInt %)
;;       :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
;;      ;; A non-idempotent option (:default is applied first)
;;      ["-v" nil "Verbosity level"
;;       :id :verbosity
;;       :default 0
;;       :update-fn inc] ; Prior to 0.4.1, you would have to use:
;;      ;; :assoc-fn (fn [m k _] (update-in m [k] inc))
;;      ;; A boolean option defaulting to nil
;;      ["-h" "--help"]])

;;   (defn -main [& args]
;;     (parse-opts args cli-options)))

(ns cyclotron.visualization.components)

(def disabled-gray "#707070")

(def error-orange "#fc9403")

(def failure-red "#db3b21")

(def success-green "#1aaa55")

(def color {:disabled disabled-gray
            :error error-orange
            :failure failure-red
            :success success-green})


;; (comment

;;   (require '[cyclotron.run :as run]
;;            '[oz.core :as oz])

;;   (oz/start-plot-server!)

;;   (create-ns 'cyclotron.run.count)

;;   (alias 'stats 'cyclotron.run.count)

;;   (alias 'run 'cyclotron.run)

;;   (defn successes [meas]
;;     (max 0 (- (::stats/specs meas)
;;               (+ (::stats/errors meas)
;;                  (::stats/failures meas)
;;                  (::stats/disabled meas)))))

;;   (defn measurements [run]
;;     (let [meas (run/measure run)]
;;       (-> (assoc meas ::stats/successes (successes meas))
;;           (select-keys [::run/date
;;                         ::run/pipeline
;;                         ::stats/specs
;;                         ::stats/errors
;;                         ::stats/disabled
;;                         ::stats/failures
;;                         ::stats/successes
;;                         ::run/elapsed-time]))))

;;   (defn observe-key [k run]
;;     {:pipeline (::run/pipeline run)
;;      :count (get run k)
;;      :result k})

;;   (defn observations [runs]
;;     (for [r (map measurements runs)
;;           k [:cyclotron.run.count/failures
;;              :cyclotron.run.count/successes
;;              :cyclotron.run.count/errors
;;              :cyclotron.run.count/disabled]]
;;       (observe-key k r)))

;;   (def disabled-gray "#707070")
;;   (def error-orange "#fc9403")
;;   (def failure-red "#db3b21")
;;   (def success-green "#1aaa55")

;;   (def fremove (comp first remove))


;;   (defn stacked-bar [runs]
;;     (let [values (observations runs)]
;;       {:title (or (first (->> runs
;;                               (map ::run/date)
;;                               (remove nil?))) nil)
;;        :data {:values values}
;;        :mark "bar"
;;        :encoding {:x {:field "pipeline"
;;                       :type "ordinal"}
;;                   :y {:aggregate "sum"
;;                       :field "count"
;;                       :type "quantitative"}
;;                   :color {:field "result"
;;                           :type "nominal"
;;                           :scale {:domain ["disabled" "errors" "failures" "successes"]
;;                                   :range [disabled-gray error-orange failure-red success-green]}}}}))

;;   (oz/v! (stacked-bar (take 6 (remove #(nil? ( ::run/date %) ) run/runs))))

;;   ;; (def partitioned-runs (partition 5 run/runs))

;;   (def partitioned-runs
;;     (->> run/runs
;;          (remove #(nil? (::run/date %)))
;;          (partition-by ::run/date)))

;;   (set! *print-length* 10)

;;   (defn row-of-multiples [row]
;;     [:div {:style {:display "flex" :flex-direction "row"}}
;;      (map (fn [part]
;;             [:vega-lite (stacked-bar (nth partitioned-runs part))]) (range (* row 4) (* (+ 1 row ) 4)))])

;;   (oz/view! [:div
;;              [:h1 "Daily Pipelines"]
;;              (row-of-multiples 0)
;;              (row-of-multiples 1)
;;              (row-of-multiples 2)]
;;             [:h3 "History"]
;;             [:vega-lite (dissoc (stacked-bar (take 50 run/runs)) :title)])
;;   )

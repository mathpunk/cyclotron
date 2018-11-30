;; (ns cyclotron.review)


;; (comment
;;   (load))

;; (def cached
;;   (->> reports-cache
;;        file-seq
;;        (filter #(.isFile %))))

;; (defn stage? [file]
;;   (re-find #"stage-e2e" (.getPath file)))

;; (def stage
;;   (filter stage? cached))

;; (defn fresh? [file]
;;   (let [path (.getPath file)
;;         date (time/format "yyyy/MM/dd" (time/local-date))]
;;     (re-find (re-pattern date) path)))

;; (def nightly-stage
;;   (->> cached
;;        (filter stage?)
;;        (filter fresh?)))

;; (defn stable? [file]
;;   (re-find #"test-e2e" (.getPath file)))

;; (def nightly-test
;;   (->> cached
;;        (filter stable?)
;;        (filter fresh?)))

;; (defn run-date [report-file]
;;   (re-find #"\d\d\d\d/\d\d/\d\d" (.getPath report-file)))

;; (defn job-name [report-file]
;;   (let [path (.getPath report-file)
;;         date (run-date report-file)]
;;     (string/join (take-while #(not= \/ %) (drop 11 (drop (string/index-of path date) path))))))

;; (defn pipeline-id [report-file]
;;   (let [path (.getPath report-file)]
;;     (second (re-find #"pipeline-(\d+)" path))))

;; (defn revision-sha [report-file]
;;   (let [path (.getPath report-file)]
;;     (second (re-find #"revision-(\w+)" path))))

;; (defn create-report [report-file]
;;   {:cyclotron.run/date (run-date report-file)
;;    :cyclotron.run/pipeline (pipeline-id report-file)
;;    :cyclotron.run/revision (revision-sha report-file)
;;    :cyclotron.run/job (job-name report-file)
;;    :cyclotron.run/data (data/parse-str (slurp report-file))
;;    })

;; (comment "Sanity check"
;;          (set! *print-length* 10)

;;          (set! *print-level* 2)

;;          nightly-test

;;          (def report-file (first nightly-test))

;;          (.getPath report-file)
;;          ;; what you'd expect

;;          (pipeline-id report-file)
;;          ;; what you'd expect

;;          (revision-sha report-file)
;;          ;; what you'd expect
;;          )






;; (defn failure-summary [failure]
;;   (-> failure
;;       :attrs
;;       (set/rename-keys {:type :cyclotron.report-data.failure/type
;;                         :message :cyclotron.report-data.failure/message})
;;       (merge {:cyclotron.report-data.failure/stacktrace (-> failure
;;                                                             :content
;;                                                             first)})))

;; (s/fdef failure-summary
;;   :args ::failure)

;; (defn case-summary [case]
;;   (let [str->float (fn [s] (Double. s))]
;;     (-> (:attrs case)
;;         (update :time str->float)
;;         (set/rename-keys {:classname :cyclotron.report-data.case/context
;;                           :name :cyclotron.report-data.case/assertion
;;                           :time :cyclotron.report-data.case/time})
;;         (assoc :cyclotron.report-data/failures (map failure-summary (:content case))))))

;; (s/fdef case-summary
;;   :args ::case)

;; (defn suite-summary [suite]
;;   (let [str->int (fn [s] (Integer. s))
;;         str->float (fn [s] (Double. s))]
;;     (-> (:attrs suite)
;;         (update :errors str->int)
;;         (update :disabled str->int)
;;         (update :tests str->int)
;;         (update :failures str->int)
;;         (update :skipped str->int) ;; How is :disabled different from :skipped?
;;         (update :time str->float)
;;         (set/rename-keys {:errors :cyclotron.report-data.suite/errors
;;                           :disabled :cyclotron.report-data.suite/disabled
;;                           :tests :cyclotron.report-data.suite/specs
;;                           :failures :cyclotron.report-data.suite/failures
;;                           :skipped :cyclotron.report-data.suite/skipped
;;                           :time :cyclotron.report-data.suite/time
;;                           :name :cyclotron.report-data.suite/context
;;                           :hostname :cyclotron.report-data.suite/hostname
;;                           :timestamp :cyclotron.report-data.suite/timestamp})
;;         (assoc :cyclotron.report-data/cases (map case-summary (:content suite))))))

;; (s/fdef suite-summary
;;   :args ::suite)

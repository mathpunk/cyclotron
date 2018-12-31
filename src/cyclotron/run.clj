(ns cyclotron.run
  (:require [clojure.spec.alpha :as s]
            [cyclotron.cache :as cache]
            [clojure.data.xml :as xml]
            [clojure.set :as set]))


;; Parsing into maps
;; =========================
(defn date [path]
  (second (re-find #"reports/(\d\d\d\d/\d\d/\d\d)/" path)))

(defn job [path]
  (second (re-find #"\d\d\d\d/\d\d/\d\d/([\w-]+)" path)))

(defn pipeline [path]
  (second (re-find #"pipeline-(\d+)" path)))

(defn suites
  "If known, otherwise nil"
  [path]
  (if-let [suite-string (re-find #"suites-(\w+,?)+" path)]
    (-> suite-string
        first
        (string/split #"suites-")
        second
        (string/split #","))
    nil))

(defn revision [path]
  (second (re-find #"revision-(\w+)" path)))

(defn run-meta [path]
  {::date (date path)
   ::pipeline (pipeline path)
   ::revision (revision path)
   ::suites (suites path)
   ::job (job path)})

(defn run-data [file]
  {::data (xml/parse-str (slurp file))})

(defn create-run [file]
  (let [path (.getPath file)]
    (merge (run-data file)
           (run-meta path))))


;; JUnit has some statistics about the run (tho, a little weird)
;; ==============================================================
(defn evaluate-run [file]
  (let [run (create-run file)
        xml (::data run)
        str->int (fn [s] (Integer. s))
        str->float (fn [s] (Double. s))]
    (merge run (-> (:attrs xml)
                   (update :disabled str->int)
                   (update :errors str->int)
                   (update :failures str->int)
                   (update :tests str->int)
                   (update :time str->float)
                   (set/rename-keys {:disabled :cyclotron.run.count/disabled
                                     :errors   :cyclotron.run.count/errors
                                     :failures :cyclotron.run.count/failures
                                     :tests    :cyclotron.run.count/specs
                                     :time     :cyclotron.run/elapsed-time})))))


;; The collection
;; =====================
(def runs
  (->> cache/cache
       (map evaluate-run)
       (sort-by :cyclotron.run/pipeline)
       reverse))







;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Validation. I don't believe it.


;; (s/def ::testsuite-xml
;;   (fn [xml]
;;     (every? (fn [tag] (= tag :testsuite)) (map :tag (:content xml)))))

;; (defn valid-xml? [run]
;;   (s/valid? ::testsuite-xml (:cyclotron.run/data run)))

;; (valid-xml? (first all-runs))

;; (every? valid-xml? all-runs)


;; (defn validate-data [data]
;;   (if (s/valid? ::testsuite-xml data)
;;     data
;;     ::invalid-xml))





;; (defn complain [rep e]
;;   (println "Dropping data in pipeline" (::pipeline rep))
;;   (println "Invalid XML data: " (.getMessage e)))

;; (defn validate-report [rep]
;;   (try (update rep ::data validate-data)
;;        (catch Exception e
;;          (do
;;            (complain rep e)
;;            (assoc rep ::data ::invalid-xml)))))


;; (def runs
;;   (let [] (->> all-runs
;;                (map validate-report)
;;                (remove #(= ::invalid-xml (:cyclotron.run/data %))))))






;; (def ffilter (comp first filter))

;; (defn pipeline [runs id]
;;   (ffilter #(= (str id) (:cyclotron.run/pipeline %) ) runs))

;; (defn date [runs date]
;;   (filter #(= date (:cyclotron.run/date %)) runs))

;; (comment
;;   (require '[clojure.pprint :refer [pprint]])

;;   (set! *print-length* 10)

;;   (set! *print-level* 4)

;;   (defn count-cases [run]
;;     (->> (suites (::data run))
;;          (map cases)
;;          (map count)
;;          (reduce +)))

;;   (map measure runs)

;;   ((juxt count-cases :cyclotron.run.count/failures) (measure (first runs)))

;;   (every? (fn [[case fail]] (> case fail))
;;           (map (juxt count-cases :cyclotron.run.count/failures) (map measure  runs)))

;;   ;; Proof that I'm not counting cases right :feelsbadman:


;;   )







;; (comment
;;   (pprint (measure (first runs)))
;;   (pprint (measurements (first runs)))
;;   )


;; (comment "Used in developing the above, but not seemingly important itself..."
;;          #_(defn passing? [report]
;;              (= (:cyclotron.run.statistic/failures (summary report)) 0))

;;          (s/def ::case
;;            (s/or :succeeded nil? ;; Seems dangerous...
;;                  :did-not-succeed (fn [xml] (= :testcase (:tag xml)))))

;;          (s/def ::suite
;;            (s/and (fn [xml] (= :testsuite (:tag xml)))
;;                   (fn [xml] (every? #(s/valid? ::case %) (:content xml)))))

;;          (s/def ::suites
;;            (s/and (fn [xml] (= :testsuites (:tag xml)))
;;                   (fn [xml] (every? #(s/valid? ::suite %) (:content xml)))))

;;          (s/def ::stacktrace string?)

;;          (s/def ::failure
;;            (s/and (fn [xml] (= :failure (:tag xml)))
;;                   (fn [xml] (s/valid? ::stacktrace (first (:content xml))))
;;                   (fn [xml] (= 1 (count (:content xml))))))

;;          ;; (defn has-failures? [testsuite]
;;          ;;   (let [failure-count (Integer. (get-in testsuite [:attrs :failures]))]
;;          ;;     (> failure-count 0)))

;;          ;; (s/fdef has-failures?
;;          ;;   :args ::suite)

;;          (defn suites [xml]
;;            (:content xml))

;;          (s/fdef suites
;;            :args #(= :testsuites (:tag %)))

;;          (defn cases [suite]
;;            (:content suite)) ;; A little unsure. Things can succeed and have no cases. Maybe always?

;;          (s/fdef cases
;;            :args ::suite)

;;          (defn details [case]
;;            (:content case)) ;; Not sure about this one. It seems like it's empty for success, non-empty for skipped/disabled/failed

;;          (s/fdef details
;;            :args ::case))

;; (comment

;;   (def ffilter (comp first filter))

;;   (def failing-run (ffilter has-failures? runs))

;;   (first runs)

;;   )

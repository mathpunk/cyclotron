(ns cyclotron.report-data
  "Validating and summarizing test suite collections, test suites, test cases, and test failures."
  (:require [cyclotron.run :as test-runs]
            [clojure.spec.alpha :as s]
            [clojure.set :as set]))


(s/def ::case
  (s/or :succeeded nil? ;; Seems dangerous...
        :did-not-succeed (fn [xml] (= :testcase (:tag xml)))))

(s/def ::suite
  (s/and (fn [xml] (= :testsuite (:tag xml)))
         (fn [xml] (every? #(s/valid? ::case %) (:content xml)))))

(s/def ::suites
  (s/and #_(fn [xml] (= :testsuites (:tag xml)))
         (fn [xml] (every? #(s/valid? ::suite %) (:content xml)))))

(s/def :cyclotron.report-data.failure/stacktrace string?)

(s/def ::failure
  (s/and #_(fn [xml] (= :failure (:tag xml)))
         (fn [xml] (s/valid? :cyclotron.report-data.failure/stacktrace (first (:content xml))))
         (fn [xml] (= 1 (count (:content xml))))))

(defn has-failures? [testsuite]
  (let [failure-count (Integer. (get-in testsuite [:attrs :failures]))]
    (> failure-count 0)))

(s/fdef has-failures?
  :args ::suite)

(defn suites [xml]
  (:content xml))

(s/fdef suites
  :args #(= :testsuites (:tag %)))

(defn cases [suite]
  (:content suite)) ;; A little unsure. Things can succeed and have no cases. Maybe always?

(s/fdef cases
  :args ::suite)

(defn details [case]
  (:content case)) ;; Not sure about this one. It seems like it's empty for success, non-empty for skipped/disabled/failed

(s/fdef details
  :args ::case)

(defn failure-summary [failure]
  (-> failure
      :attrs
      (set/rename-keys {:type :cyclotron.report-data.failure/type
                        :message :cyclotron.report-data.failure/message})
      (merge {:cyclotron.report-data.failure/stacktrace (-> failure
                                                     :content
                                                     first)})))

(s/fdef failure-summary
  :args ::failure)

(defn case-summary [case]
  (let [str->float (fn [s] (Double. s))]
    (-> (:attrs case)
        (update :time str->float)
        (set/rename-keys {:classname :cyclotron.report-data.case/context
                          :name :cyclotron.report-data.case/assertion
                          :time :cyclotron.report-data.case/time})
        (assoc :cyclotron.report-data/failures (map failure-summary (:content case))))))

(s/fdef case-summary
  :args ::case)

(defn suite-summary [suite]
  (let [str->int (fn [s] (Integer. s))
        str->float (fn [s] (Double. s))]
    (-> (:attrs suite)
        (update :errors str->int)
        (update :disabled str->int)
        (update :tests str->int)
        (update :failures str->int)
        (update :skipped str->int) ;; How is :disabled different from :skipped?
        (update :time str->float)
        (set/rename-keys {:errors :cyclotron.report-data.suite/errors
                          :disabled :cyclotron.report-data.suite/disabled
                          :tests :cyclotron.report-data.suite/specs
                          :failures :cyclotron.report-data.suite/failures
                          :skipped :cyclotron.report-data.suite/skipped
                          :time :cyclotron.report-data.suite/time
                          :name :cyclotron.report-data.suite/context
                          :hostname :cyclotron.report-data.suite/hostname
                          :timestamp :cyclotron.report-data.suite/timestamp})
        (assoc :cyclotron.report-data/cases (map case-summary (:content suite))))))

(s/fdef suite-summary
  :args ::suite)

(comment "Load for experimentation. You'll need to locate failing and succeeding suites."

         (require '[clojure.spec.alpha :as s]
                  '[clojure.spec.test.alpha :as t]
                  '[clojure.spec.gen.alpha :as g]
                  '[clojure.string :as string]
                  '[clojure.pprint :refer [print-table pprint]]
                  '[cognitect.transcriptor :refer [check!]]
                  '[java-time :as time]
                  '[clojure.java.io :as io]
                  '[clojure.data.xml :as data]
                  '[xml-in.core :as xin :refer [tag=]])

         ;; (eet! *print-length* 12)
         ;;   (require '[clojure.spec.alpha :as s]
         ;;            '[clojure.spec.test.alpha :as t]
         ;;            '[clojure.spec.gen.alpha :as g]
         ;;            '[clojure.string :as string]
         ;;            '[clojure.pprint :refer [print-table pprint]]
         ;;            '[cognitect.transcriptor :refer [check!]]
         ;;            '[java-time :as time]
         ;;            '[clojure.java.io :as io]
         ;;            '[clojure.data.xml :as data]
         ;;            '[xml-in.core :as xin :refer [tag=]])

         ;; (set! *print-level* 2)

         ;; (reports/load)

         ;; (def run test-runs/latest-test)


         (keys run)

         (pprint run)

         (type (:cyclotron.run/data run))

         (def xml (:cyclotron.run/data run))

         (check! ::suites xml)

         (def succeeding (nth (:content xml) 0))

         (check! ::suite succeeding)

         (def failing (nth (:content xml) 4))

         (check! ::suite failing)

         failing

         (:tag failing)

         (map :tag (:content failing))

         (count (:content failing))

         (s/valid? ::case (first (:content failing)))

         (s/valid? ::failure (first (:content (first (:content failing)))))

         (failure-summary (first (:content (first (:content failing))))))

(comment "Verify"

         (def f-case (-> failing
                         :content
                         first))

         (check! ::case f-case)

         (def s-case (-> succeeding
                         :content
                         first))

         (check! ::case s-case)

         (check! #{true} (every? #(s/valid? ::suite %) (suites xml)))

         (def failures (filter has-failures? (suites xml)))

         (check! ::suites failures)

         (check! #{true} (every? #(s/valid? ::suite %) failures))

         (def successes (remove has-failures? (suites xml)))

         (check! ::suites successes)

         (check! #{true} (every? #(s/valid? ::suite %) successes))

         (check! #{true} (every? #(s/valid? ::case %) (:content (nth successes 1))))

         ;; And then put failures in if there are any?

         ;; (set! *print-level* nil)

         ;; (set! *print-length* 10)

         (pprint (nth successes 1))
         ;; Great but, let's get pessimistic and stop caring about successes today.

         (pprint (nth failures 0))

         (let [case (nth failures 0)]
           (every? #(s/valid? ::case %) (:content case)))

         (let [failure (nth failures 0)]
           (pprint (map case-summary (:content failure))))

         (let [failure (nth failures 0)]
           (count (:content failure)))

         (every? #(s/valid? ::suite %)  failures )

         (pprint (map suite-summary failures)))

(comment "Initial exploration of the report (xml) structure"

         report

         (def xml (:cyclotron.reports/content report))

         (keys xml)

         (-> xml
             :tag)

         (-> xml
             :attrs)

         (defn summary-of-run [xml]
           (:attrs xml))

         (summary-of-run xml)

         (-> xml
             :content
             count) ;; Number of suites. But, don't really see us caring?

         (s/def ::test-run-data
           (s/and (fn [xml]
                    (every? (fn [tag] (= tag :testsuite)) (map :tag (:content xml))))))

         (s/explain ::test-run-data xml)

         (->> (:content xml)
              (map #(get-in % [:attrs :name] )))


         (has-failures? (nth (:content xml) 0))

         (has-failures? (nth (:content xml) 1))

         (has-failures? (nth (:content xml) 2))

         (def failures
           (filter has-failures? (:content xml)))

         ;; merge report :cyclotron.reports/summary summary-of-run
         ;; merge report :cyclotron.reports/failures failures

         (type (first failures))

         (:tag (first failures))

         (:attrs (first failures))

         (-> (first failures)
             :content
             first
             :tag) ;; => :testcase

         (-> (first failures)
             :content
             first
             :attrs) ;; =>

         {:classname "Incident Management --- Assignment Completion:.when creating and completing an assignment,.the page", :name "shows a \"Complete\" button", :time "15.047"}

         (-> (first failures)
             :content
             first
             :content
             first
             :tag) ;; => :failure

         (-> (first failures)
             :content
             first
             :content
             first
             :attrs)
         ;; =>
         {:type "exception", :message "Failed: Wait timed out after 15002ms"}

         (type (-> (first failures)
                   :content
                   first
                   :content
                   first
                   :content
                   first))
         ;; The stacktrace!
         )

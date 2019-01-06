(ns cyclotron.suite
  (:require [clojure.set :as set]
            [cyclotron.utils :refer [str->int str->float]]
            [java-time.local :refer [local-date-time]]))

(defn parse-suite-attrs [attrs]
  (-> attrs
      (update :errors str->int)
      (update :disabled str->int)
      (update :tests str->int)
      (update :time str->float)
      (dissoc :hostname)
      (update :skipped str->int)
      (update :timestamp local-date-time)
      (update :failures str->int)
      (set/rename-keys {:errors     :cyclotron.suite.count/errors
                        :disabled   :cyclotron.suite.count/disabled
                        :tests      :cyclotron.suite.count/tests
                        :name       :cyclotron.suite/name
                        :time       :cyclotron.suite/elapsed-time
                        :skipped    :cyclotron.suite.count/skipped
                        :timestamp  :cyclotron.suite/timestamp
                        :failures   :cyclotron.suite.count/failures})))

(defn case-groups [testsuites-xml]
  (->> testsuites-xml
       :content
       (map :content)
       (remove empty?)))

(defn describe-case [case-xml]
  {:cyclotron.case/precondition (get-in case-xml [:attrs :classname])
   :cyclotron.case/expectation (get-in case-xml [:attrs :name])})

(defn describe-suite [testsuite-xml]
  (-> testsuite-xml
      (dissoc :tag)
      (update :attrs parse-suite-attrs)
      (update :content #(map describe-case %))
      (set/rename-keys {:content :cyclotron.suite/cases
                        :attrs :cyclotron.suite/attributes})))

(defn suites [run]
  (map describe-suite (get-in run [:cyclotron.run/report :content])))

(defn cases [run]
  (let [suites (suites run)]
    (mapcat :cyclotron.suite/cases suites)))

(defn- preconditioned-expectations [partitioned-case]
  {:cyclotron.case/precondition (:cyclotron.case/precondition (first partitioned-case))
   :cyclotron.case/expectations (map :cyclotron.case/expectation partitioned-case)})

(defn expectations [run]
  (->> run
       cases
       (partition-by :cyclotron.case/precondition)
       (map preconditioned-expectations)))


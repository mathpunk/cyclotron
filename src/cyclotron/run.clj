(ns cyclotron.run
  (:require [cyclotron.reports :as reports]
            [clojure.set :as set]
            [cognitect.transcriptor :refer [check!]]
            [clojure.spec.alpha :as s]))


(s/def ::expected-data-form
  (fn [xml]
    (every? (fn [tag] (= tag :testsuite)) (map :tag (:content xml)))))

;; (check! ::expected-data-form (some-report :cyclotron.run/data))

(def all-cached
  (map reports/create-report reports/cached))

(defn summary [report]
  (let [xml (::data report)
        str->int (fn [s] (Integer. s))
        str->float (fn [s] (Double. s))]
    (merge report (-> (:attrs xml)
                      (update :disabled str->int)
                      (update :errors str->int)
                      (update :failures str->int)
                      (update :tests str->int)
                      (update :time str->float)
                      (set/rename-keys {:disabled :cyclotron.run.statistics/disabled
                                        :errors   :cyclotron.run.statistics/errors
                                        :failures :cyclotron.run.statistics/failures
                                        :tests    :cyclotron.run.statistics/specs ;; like test cases?
                                        :time     :cyclotron.run.statistics/time})))))

(defn passing? [report]
  (= (:cyclotron.run.statistics/failures (summary report)) 0))


(comment "Get a report, loading if necessary"

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

    (set! *print-length* 12)

    (set! *print-level* 2)

    #_(reports/load)

    #_(def latest-report
        (reports/create-report (first reports/nightly-test)))

    reports/cached

    #_(def latest-report
        (reports/create-report (first reports/nightly-test)))

    (def some-report
      (reports/create-report (first reports/cached)))

    (def latest-test
      (summary latest-report)))


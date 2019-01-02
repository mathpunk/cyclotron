(ns user)

(do "Setup"

    (require '[clojure.spec.alpha :as s]
             '[clojure.spec.test.alpha :as t]
             '[clojure.spec.gen.alpha :as g]
             '[clojure.string :as string]
             '[clojure.java.io :as io]
             '[clojure.pprint :refer [print-table pprint]]
             '[cognitect.transcriptor :refer [check!]])

    (set! *print-length* 10)

    (set! *print-level* 4)

    #_(cache/cache init)
    )


(require '[cyclotron.run :as run])
(require '[cyclotron.utils :refer [str->int str->float]])

(pprint (->> run/runs
             (filter run/successful?)
             (sort-by :cyclotron.run/pipeline)
             reverse
             (filter #(> (:cyclotron.run.count/specs %) 200))
             (take 1)))

;; One nice run, 111 specs passed: https://gitlab.logicgate.com/platform/logicgate/pipelines/14454

;; One nice run, nodes suite, 202 specs passed: https://gitlab.logicgate.com/platform/logicgate/pipelines/14898


(s/def ::testsuite-xml
  (fn [xml]
    (every? (fn [tag] (= tag :testsuite)) (map :tag (:content xml)))))

(def ffilter (comp first filter))

(defn pipeline [runs id]
  (ffilter #(= (str id) (:cyclotron.run/pipeline %) ) runs))

(require '[java-time.core :as time])

(require '[java-time.local :refer [local-date-time]])

(require '[clojure.set :as set])

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
  (map describe-suite (get-in run [:cyclotron.run/data :content])))

(defn cases [run]
  (let [suites (suites run)]
    (mapcat :cyclotron.suite/cases suites)))


(defn preconditioned-expectations [g]
  {:cyclotron.case/precondition (:cyclotron.case/precondition (first g))
   :cyclotron.case/expectations (map :cyclotron.case/expectation g)})

(defn expectations [run]
  (->> run
       cases
       (partition-by :cyclotron.case/precondition)
       (map preconditioned-expectations)))

(pprint (let [success-id "14453"
              success-run (pipeline run/runs success-id)
              run success-run]
          (expectations run)))

'(#:cyclotron.case{:precondition "Process:.a new process.with a new workflow,.the workflow nodes",
                   :expectation "exist by default"}
  #:cyclotron.case{:precondition "Process:.a new process.with a new workflow,.the workflow nodes",
                   :expectation "lead to node pages"}
  #:cyclotron.case{:precondition "Process:.a new process.with a new workflow,.the workflow nodes",
                   :expectation "can open the editor for a node"})

(pprint (let [e2e-successful-id "14454"
              nodes-successful-id "14898"
              run (pipeline run/runs e2e-successful-id)]
          (expectations run)))


(comment "so if I understand this right, the xml data is a :testsuites, and it has some
statistics and :content. The :content is a collection of :testsuite items. THOSE have
attrs. And their content, if any, are these case groups. ")


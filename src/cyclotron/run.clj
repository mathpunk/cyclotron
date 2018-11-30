(ns cyclotron.run
  (:require [cyclotron.artefact :as artefact]
            [cyclotron.cache :as cache]
            [clojure.data.xml :as xml]
            [clojure.set :as set]))

(defn run-meta [file]
  {::date (artefact/date (.getPath file))
   ::pipeline (artefact/pipeline (.getPath file))
   ::revision (artefact/revision (.getPath file))
   ::job (artefact/job (.getPath file))})

(defn run-data [file]
  (xml/parse-str (slurp file)))

(defn create-run [file]
  (assoc (run-meta file) ::data (run-data file)))

(def all-runs
  (reverse (sort-by :cyclotron.run/pipeline (map create-run cache/cache))))

(s/def ::testsuite-xml
  (fn [xml]
    (every? (fn [tag] (= tag :testsuite)) (map :tag (:content xml)))))

(defn validate-data [data]
  (if (s/valid? ::testsuite-xml data)
    data
    ::invalid-xml))

(defn complain [rep e]
  (println "Dropping data in pipeline" (::pipeline rep))
  (println "Invalid XML data: " (.getMessage e)))

(defn validate-report [rep]
  (try (update rep ::data validate-data)
       (catch Exception e
         (do 
           (complain rep e)
           (assoc rep ::data ::invalid-xml)))))

(def runs
  (let []
    (map validate-report all-runs)))

(defn measure [run]
  (let [xml (::data run)
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

(defn measurements [run]
  (select-keys (measure run ) [::date
                               :cyclotron.run.count/specs
                               :cyclotron.run.count/errors
                               :cyclotron.run.count/disabled
                               :cyclotron.run.count/failures
                               ::elapsed-time]))




(comment
  (pprint (measure (first runs)))
  (pprint (measurements (first runs)))
  )


(comment "Used in developing the above, but not seemingly important itself..."
  (defn passing? [report]
    (= (:cyclotron.run.statistic/failures (summary report)) 0))

  (s/def ::case
    (s/or :succeeded nil? ;; Seems dangerous...
          :did-not-succeed (fn [xml] (= :testcase (:tag xml)))))

  (s/def ::suite
    (s/and (fn [xml] (= :testsuite (:tag xml)))
           (fn [xml] (every? #(s/valid? ::case %) (:content xml)))))

  (s/def ::suites
    (s/and (fn [xml] (= :testsuites (:tag xml)))
           (fn [xml] (every? #(s/valid? ::suite %) (:content xml)))))

  (s/def ::stacktrace string?)

  (s/def ::failure
    (s/and (fn [xml] (= :failure (:tag xml)))
           (fn [xml] (s/valid? ::stacktrace (first (:content xml))))
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
    :args ::case))

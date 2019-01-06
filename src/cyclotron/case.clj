(ns cyclotron.case
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [cyclotron.failure :as failure]))


(defn- content?
  [node]
  (not (empty? (:content node))))

(defn- uniform-content? [node tag]
  (and (content? node)
       (->> (:content node)
            (map :tag)
            (every? #(= tag %)))))

(s/def ::testcase #(= :testcase (:tag %)))

(s/def ::passing (s/and ::testcase
                        (fn [node] (not (content? node)))))

(s/def ::failing (s/and ::testcase
                        (fn [node] (uniform-content? node :failure))))

(s/def ::skipped (s/and ::testcase
                        (fn [node] (uniform-content? node :skipped))))

(s/def ::result (s/or :passed ::passing
                      :failed ::failing
                      :skipped ::skipped))

(defn case-nodes
  [report]
  (filter (partial s/valid? ::testcase) report))

(defn precondition [case-node]
  (get-in case-node [:attrs :classname]))

(defn expectation [case-node]
  (get-in case-node [:attrs :name]))

(defn elapsed-time [case-node]
  (Double. (get-in case-node [:attrs :time])))

(defn create-case [case-node]
  {:cyclotron.case/precondition (precondition case-node)
   :cyclotron.case/expectation (expectation case-node)
   :cyclotron.case/elapsed-time (elapsed-time case-node)})

(defn create-failure [failure-node]
  (merge (create-case failure-node)
         {:cyclotron.failure/events (map failure/create-event (:content failure-node))}))

(defn breakdown
  [report]
  (let [classified (group-by #(first (s/conform ::result %)) (case-nodes report))]
    (-> classified
        (update :passed #(map create-case %))
        (update :skipped #(map create-case %))
        (update :failed #(map create-failure %)))))

(defn score [report]
  (-> (breakdown report)
      (update :passed count)
      (update :skipped count)
      (update :failed count)))


(comment

  (require '[cyclotron.utils :refer [mock-run]])

  (:cyclotron.run/report mock-run)

  (keys mock-run)

  (cases mock-run)

  (breakdown mock-run)

  (score mock-run))


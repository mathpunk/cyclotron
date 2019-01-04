(ns user)

(do "Require your tools"
    (require '[clojure.spec.alpha :as s]
             '[clojure.spec.test.alpha :as t]
             '[clojure.spec.gen.alpha :as g]
             '[clojure.string :as string]
             '[clojure.java.io :as io]
             '[clojure.pprint :refer [print-table pprint]]
             '[cognitect.transcriptor :refer [check!]])

    (defn set-print
      [{:keys [level length]}]
      (do (set! *print-length* level)
          (set! *print-level* length)))

    (defn purge!
      "Unbinds all public vars. For use when you're done messing with one concept in userspace."
      []
      (map #(ns-unmap 'user %) (keys (ns-publics 'user))))

    "Possibly load the cache (in it's entirety, sigh)"
    #_(do
        (require '[cyclotron.cache :as cache])
        (cache/init))

    (set-print {:length 8 :level 3}))

(do "Require your code"
    (require '[cyclotron.run :as run])
    (require '[cyclotron.case :as case]))


(set-print {:level nil :length 10})

(do 
  (alias 'case 'cyclotron.case)
  (alias 'run 'cyclotron.run)

  (defn success-rate [{:keys [cyclotron.case/passed cyclotron.case/failed]}]
    (let [total (+ passed failed)]
      (float (/ passed total))))

  (defn summarize [run]
    (let [basic-stats (case/breakdown-count run)
          derived-stats (assoc basic-stats :cyclotron.run.stats/success-ratio (success-rate basic-stats))]
      (-> run
          (dissoc ::run/data ::run/revision)
          (merge derived-stats))))

  (->> run/runs
       (map summarize)
       (remove #(zero? (::case/passed %)))
       (take 10)
       print-table))



(comment "Scrap helpers"
  (def ffilter (comp first filter))

  (defn get-pipeline [id]
    (ffilter #(= (str id) (:cyclotron.run/pipeline %) ) runs))

  (defn successful? [run]
    (= 0 (:cyclotron.run.count/failures run)))

  (defn failing? [run]
    (not (successful? run))))

(ns cyclotron.core
  #_(:gen-class)
  #_(:require [cyclotron.reports :as reports]
            [cyclotron.run :as run]))

#_(def we-good
  (->> reports/nightly-test
       (map reports/create-report)
       (every? run/passing?)))


#_(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [caching (reports/load)]
    (if (= caching :cyclotron.reports/ok)
      (if we-good
        (println "passing!")
        (println "failing :("))
      (println "data retrieval error of some kind"))))



;; (comment "Report successful test run"
;; "- For a user"
;; "- For the fact-accumulating database"
;; )

;; (comment "Report failing test run"
;; "- For a user"
;; "- For the fact-accumulating database"
;; )

;; (comment "Get at the data in the reports--- I guess copy them to some temporary or semi-temporary directory. Pending some correctly uploaded junitresults."

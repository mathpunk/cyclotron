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
    ;; (do
    ;;     (require '[cyclotron.cache :as cache])
    ;;     (cache/init))

    (set-print {:length 8 :level 3}))

(do "Require your code"
    (require '[cyclotron.run :as run])
    (require '[cyclotron.report :as report])
    (require '[cyclotron.case :as case])
    (require '[cyclotron.code :as code]))


(do "Don't break reports"

    (report/ascii-summary-recent 10)

    (report/ascii-summary-successes 10))



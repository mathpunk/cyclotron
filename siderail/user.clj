(ns user
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as test]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.pprint :refer [print-table pprint]]
            [cognitect.transcriptor :refer [check!]]))

(do "Tools"

    (defn set-print
      [{:keys [level length]}]
      (do (set! *print-length* level)
          (set! *print-level* length)))

    (defn purge!
      "Unbinds all public vars. For use when you're done messing with one concept in userspace."
      []
      (map #(ns-unmap 'user %) (keys (ns-publics 'user))))


    (set-print {:length 8 :level 3}))

(comment

  "Possibly load the cache (in it's entirety, sigh)"
  (do
    (require '[cyclotron.cache :as cache])
    (cache/init)))

(do "Require your code"
    (require '[cyclotron.run :as run])
    (require '[cyclotron.report :as report])
    (require '[cyclotron.case :as case]))

(require '[cyclotron.code :as code])

(do "Don't break reports"

    (report/ascii-summary-recent 10)

    (report/ascii-summary-successes 10))


(do

  (require '[cyclotron.utils :refer [mock-run]])


  )


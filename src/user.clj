(ns user)

(do
  (require '[clojure.spec.alpha :as s]
           '[clojure.spec.test.alpha :as t]
           '[clojure.spec.gen.alpha :as g]
           '[clojure.string :as string]
           '[clojure.repl :as repl :refer [doc]]
           '[clojure.pprint :refer [print-table pprint]]
           '[cognitect.transcriptor :refer [check!]]
           '[java-time :as time]
           '[clojure.java.io :as io]
           '[clojure.data.xml :as data]
           '[xml-in.core :as xin :refer [tag=]])

  (comment 

    (set! *print-length* 10)
    (set! *print-level* 3))
  )


(require '[cyclotron.reports :as reports]
         '[cyclotron.run :as run])

(alias 'run 'cyclotron.run)

(def ffilter (comp first filter))

(defn pipeline [id]
  (ffilter (fn [report] (= (str id) (::run/pipeline report))) run/all-cached))


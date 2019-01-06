(ns cyclotron.cache
  (:refer-clojure :exclude [load update])
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]
            [aero.core :refer [read-config]]
            [clojure.java.shell :as sh]
            [clojure.string :as string]))

(def cache-location (io/as-file (io/resource "reports")))

(defn load
  ([date]
   (let [{{:keys [bucket profile]} :aws} (read-config "config.edn")
         target (.getPath cache-location )]
     (apply sh/sh ["aws" "--profile" profile "s3" "cp" "--recursive" (str bucket date) target])))
  ([]
   (load nil)))

(defn init []
  (let [results (load)]
    (if (empty? (:err results))
      ::ok
      {::error (:err results)})))

(def cache
  (->> cache-location
       file-seq
       (filter #(.isFile %))))

(comment

  (declare update) ;; A nice-to-have, maybe, but right now it only takes a minute or so to init.

  (defn today [] (.format (java.text.SimpleDateFormat. "yyyy/MM/dd") (new java.util.Date))))

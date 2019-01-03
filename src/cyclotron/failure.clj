(ns cyclotron.failure
  (:require [clojure.string :as string]
            [xml-in.core :as xin :refer [tag=]]))

(defn failing-cases
  [run]
  (let [xml (:cyclotron.run/data run)]
    (xin/find-all (xin/find-in xml [(tag= :testsuites)
                                    (tag= :testsuite)
                                    (tag= :testcase)]) [:failure])))

(def is-spec? (partial re-find #"e2e/specs"))

(def is-page? (partial re-find #"e2e/pages"))

(defn parse-stacktrace-line [line]
  (let [index (string/index-of line "e2e")
        lexed (string/split (->> line
                                 (drop index)
                                 (string/join)) #":")]
    (apply hash-map (interleave [::file
                                 ::line
                                 ::char] lexed))))

(defn organize-module-lines [lines]
  "Keep and label lines about our own spec or page modules"
  {::specs (map parse-stacktrace-line (filter is-spec? lines))
   ::pages (map parse-stacktrace-line (filter is-page? lines))})

(defn parse-failing-case [f]
  (let [tokens (string/split-lines f)
        error (first tokens)
        stacktrace-lines (drop 1 tokens)]
    (merge {::message error
            ::stacktrace stacktrace-lines}
           (organize-module-lines stacktrace-lines))))

(defn failures [run]
  (map parse-failing-case (failing-cases run)))

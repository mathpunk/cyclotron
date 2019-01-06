(ns cyclotron.core
  (:gen-class)
  (:require [cyclotron.cache :as cache]
            [cyclotron.run :as run]
            #_[cyclotron.visualization.components :refer [color]]
            [cyclotron.report :as report]
            #_[oz.core :as oz]))


(defn -main
  "Print a summary table of didn't-entirely-fail and recent test runs"
  [& args]
  (println "\nSUCCESSFUL")
  (report/ascii-summary-successes 10)
  (println "\nRECENT")
  (report/ascii-summary-recent 10))


;; TODO: Command line options
;; (comment

;;   (ns my.program
;;     (:require [clojure.tools.cli :refer [parse-opts]])
;;     (:gen-class))

;;   (def cli-options
;;     ;; An option with a required argument
;;     [["-p" "--port PORT" "Port number"
;;       :default 80
;;       :parse-fn #(Integer/parseInt %)
;;       :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
;;      ;; A non-idempotent option (:default is applied first)
;;      ["-v" nil "Verbosity level"
;;       :id :verbosity
;;       :default 0
;;       :update-fn inc] ; Prior to 0.4.1, you would have to use:
;;      ;; :assoc-fn (fn [m k _] (update-in m [k] inc))
;;      ;; A boolean option defaulting to nil
;;      ["-h" "--help"]])

;;   (defn -main [& args]
;;     (parse-opts args cli-options)))

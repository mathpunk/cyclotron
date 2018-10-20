(ns cyclotron.report
  "Parse a local copy of junitresults.xml into a (dope) clean data structure, called `failures`."
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as t]
            [clojure.spec.gen.alpha :as g]
            [clojure.string :as str]
            [clojure.pprint :refer [print-table pprint]]
            [cognitect.transcriptor :refer [check!]]
            [clojure.java.io :as io]
            [clojure.data.xml :as data]
            [xml-in.core :as xin :refer [tag=]]))


;; Load the data
(def local-report (io/as-file "/home/man/logicgate/dev/logicgate/platform/api/src/main/resources/static/reports"))

(def test-report (-> "example-reports" io/resource io/as-file))

(def reports {:local local-report
              :test test-report})

(comment
  (reports :test)
  )

;; Parse the data into a navigable structure
(defn ->xml [report]
  (->> (file-seq report)
       (drop 2) ;; janky
       first    ;; also janky
       slurp
       data/parse-str))


;; Examine only the failures
(defn failing [xml] ;; Questionable, might throw away stuff we could use
  (xin/find-all (xin/find-in xml [(tag= :testsuites)
                                  (tag= :testsuite)
                                  (tag= :testcase)]) [:failure]))

;; Lex the failures into maps
(defn lex-failure [f]
  (let [tokens (str/split-lines f)
        error (first tokens)
        failing (drop 1 tokens)
        stacktrace-lines (drop 1 tokens)]
    {::error error
     ::failing failing
     ::stacktrace stacktrace-lines}))

(s/def ::raw-failure string?)
(s/def ::failing (s/keys :opt [::specs ::pages]))
(s/def ::lexed-failure (s/keys :req [::error ::failing ::stacktrace]))
(s/fdef lex-failure
  :args ::raw-failure
  :ret ::lexed-failure)


(comment

  "It's too rare a string to generate naively."
  #_(s/exercise-fn lex-failure) 

  "So here's an example-based test."
  (def some-failure (first (failing (->xml (reports :test)))))
  (lex-failure some-failure)
  (check! ::lexed-failure))


;; Having lexed the failures into maps, let's filter and transform the lines, so that they're more informative. The idea:
;; 1. Remove files we didn't write
;; 2. Label them as spec or page
(defn filter-failing-modules [lines]
  "Remove lines that are not about our modules."
  (->> lines
       (remove #(re-find #"node_modules" %))
       (filter #(re-find #"platform" %))))

(def is-spec? (partial re-find #"e2e/specs"))
(def is-page? (partial re-find #"e2e/pages"))

(defn label-failing-modules [lines]
  "Divide module lines by whether they are specs, or page objects"
  {::specs (filter is-spec? lines)
   ::pages (filter is-page? lines)})


;; 3. Write a function that will transform a messy module line into a neat one.
(defn lex-module-line [line]
  (let [index (str/index-of line "platform")]
    (str/split (->> line
                    (drop index)
                    (str/join)) #":")))

(defn parse-module-line [lexed-line]
  (apply hash-map (interleave [::file ::line ::char] lexed-line)))

(def module-line-parser
  (comp parse-module-line lex-module-line))

(def module-lines-parser
  (map module-line-parser))


;; 4. Apply this parser to each line we find inside [::failing ::specs] and [::failing ::pages]
(defn parse-failure [f]
  (-> f
      lex-failure
      (update-in [::failing] label-failing-modules)
      (update-in [::failing ::specs] #(map module-line-parser %))
      (update-in [::failing ::pages] #(map module-line-parser %))))


;; 5. Collect into a collection of parsed failures. 
(defn failures [report]
  (map parse-failure (failing (->xml report ))))

(comment
  (failures (reports :test))
  (def fs (failures ( reports :test )))
  (count fs)
  (check! #{46}) ;; From the test fixture
  )


;; Summarizing
;; ======================
;; Great. Now we turn that data into a nice map for summary output.

(defn group-by-failure [key f]
  (->> (get-in f [:cyclotron.report/failing key])
       (partition-by :cyclotron.report/file)))

(def group-by-failing-spec
  (partial group-by-failure :cyclotron.report/specs))

(comment
  (def fs (failures (reports :test)))
  (def f (first fs))
  (group-by-failing-spec f)
  )

(defn summarize-failing-specs [f]
  (let [parts (group-by-failing-spec f)]
    (if (= (count parts) 1)
      {:failing-spec (:cyclotron.report/file (first ( first parts)))
       :lines (sort (map #(get-in % [:cyclotron.report/line]) (first parts)))}
      (throw "Your data model is wrong: More than one spec involved in this failure somehow"))))

(def group-by-failing-page
  (partial group-by-failure :cyclotron.report/pages))

(defn summarize [f]
  (let [m (summarize-failing-specs f)]
    (assoc m :page-objects-involved (group-by-failing-page f))))

(comment

  (set! *print-length* 8)
  (def fs (failures (reports :test)))
  "Some failure"
  (def f (first fs))
  "Find a failure with a failing page "
  (dissoc (nth fs 1) :cyclotron.report/stacktrace)
  (def pf (nth fs 1))

  (group-by-failing-page pf)
  (pprint (summarize f))
  (pprint (summarize pf))
  )


(defn summary [key]
  (map summarize (failures (reports key))))



(comment
  (set! *print-length* 6)
  (pprint (summary :test))
  )

(comment "Checking failure parsing"

         (def f (first (failing xml)))
         (parse-failure f)
         (check! ::failure))

(comment "Could use some attention to clj.specs"

         ;; Not sure these specs are uptodate.
         (s/def ::spec (s/keys :req [::file ::char ::line]))
         (s/def ::failure (s/keys :req [[::error ::stacktrace ::failing]])))

(comment "Now, how would you use it in reporting? (WIP)"

         ;; Report it? Idk about this, command line utility I guess...

         (def f (first failures))

         (defn out [f]
           (let [file (get-in f [::spec ::file])
                 line (get-in f [::spec ::line])
                 message (get-in f [::error])]
             (println (str "Failure: " file ":" line))
             (println (str "Message: \"" message "\""))
             (println)))

         (out f)

         ;; Would be great to filter by what specs failing?
)

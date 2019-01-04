(ns cyclotron.failure
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(alias 'event (create-ns 'cyclotron.failure.event))

(s/def ::event-node-content (s/or :string string?
                                  :coll (s/coll-of string? :count 1)))

(defn stacktrace
  "A stacktrace is presented in the JUnit report as a collection with one string containing newline characters. This function gets that out of the event-node, verifies that the preceding assumption holds, and splits up the string."
  [event-node]
  (let [stacktrace-data (:content event-node)
        conformed (s/conform ::event-node-content stacktrace-data)]
    (case (first conformed)
      :string (string/split-lines (second conformed))
      :coll (string/split-lines (first (second conformed)))
      (throw (Exception. (str ":cyclotron.failure.event content (stacktrace-data) does not conform to spec:"
                              (s/explain ::event-node-content stacktrace-data)))))))

(alias 'point (create-ns 'cyclotron.failure.point))

(s/def ::spec-path (partial re-find #"e2e/specs"))

(s/def ::page-path (partial re-find #"e2e/pages"))

(s/def ::e2e-code (s/or ::point/pages ::page-path
                        ::point/specs ::spec-path))

(defn parse-location
  "A 'location' is a point of failure, represented as a string with a usually-uninformative symbol about what failed (e.g., Suite.<anonymous>, Object.<anonymous>), the path (with the gitlab runner as root), and line and char number."
  [loc]
  (let [pattern #"(platform/client/[\w.\-/]+):(\d+):(\d+)"
        [_ path line char] (re-find pattern loc)]
    {::point/path path
     ::point/line (Integer. line)
     ::point/char (Integer. char)}))

(defn modules
  [stacktrace]
  (let [locations (rest stacktrace)
        locations-of-interest (->> locations
                                   (remove #(re-find #"node_modules" %))
                                   (filter #(re-find #"platform/client" %)))
        points-of-failure (group-by (fn [loc] (first (s/conform ::e2e-code loc))) locations-of-interest)
        module-data (-> points-of-failure
                        (update ::point/pages #(map parse-location %))
                        (update ::point/specs #(map parse-location %)))]
    module-data))

(defn create-event [event-node]
  (let [base-event {::event/message (get-in event-node [:attrs :message])
                    ::event/type (get-in event-node [:attrs :type])}
        stacktrace (stacktrace event-node)
        error (first stacktrace)]
    (merge base-event
           {::event/error error
            ::event/stacktrace stacktrace
            ::event/modules (modules stacktrace)})))

(defn events [failure]
  (map create-event (:content failure)))


(comment "Possibility: Gitlab links from failure points"

         "Here is the gitlab url for panel.ts, line 27"
         "https://gitlab.logicgate.com/platform/logicgate/blob/master/platform/client/e2e/pages/common/panel.ts#L27"

         (defn url
           [{:keys [cyclotron.failure.point/path
                    cyclotron.failure.point/line]}]
           (let [base "https://gitlab.logicgate.com/platform/logicgate/blob/master/"]
             (str base path "#L" line)))

         (url (parse-location loc))

         "However! `platform` may vary (what if it's your own branch?) and `master` may be another branch. I don't think this rabbit is fat enough to chase."

         )

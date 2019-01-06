
(do "Figuring out counts (for ratio) once and for all!"

    ;; Preamble
    (create-ns 'cyclotron.case)
    (alias 'case 'cyclotron.case)
    (alias 'run 'cyclotron.run)
    (create-ns 'cyclotron.run.count)
    (alias 'count 'cyclotron.run.count)

    ;; Case 1: Everything passed like crazy
    (::run/suites great)
    ;; => ["nodes"]

    (failure/failing-cases great)
    (failure/failures great)
    ;; both => 0

    (count (suite/cases great))
    (::count/specs great)
    (reduce + (map (comp count ::case/expectations) (suite/expectations great)))
    ;; these all match at 202


    ;; Case 2: Let's find another nodes suite that has failures
    (::count/specs bad)
    ;; => 202

    (::count/failures bad)
    ;; => 605


    (count (suite/cases bad))

    (::count/specs bad)

    (reduce + (map (comp count ::case/expectations) (suite/expectations bad)))
    ;; HOW THO
    (count (failure/failures bad))
    (def a-failure (first (map #(dissoc % :cyclotron.failure/stacktrace) (failure/failures bad))))

    (require '[cyclotron.code :as code])

    (::run/revision bad)

    (code/checkout (::run/revision bad))

    (alias 'fail 'cyclotron.failure)

    (defn spec-coordinates [failure]
      (->> (::fail/specs failure)
           (map #(dissoc % ::fail/char))
           (map #(update % ::fail/line str->int))))

    (::fail/specs a-failure)
    (spec-coordinates a-failure)

    (def root "/home/man/logicgate/dev/logicgate/platform/client/")

    (def f a-failure)

    (require '[clojure.set :as set])


    (create-ns 'cyclotron.failure.point)

    (alias 'point 'cyclotron.failure.point))

(do "More counting"

    ;; What does junit stats say should be the number of failures? Or, bad cases anyway?
    (keys bad)

    (count (failure/failing-cases bad))

    (count (failure/failures bad))

    (count (suite/cases bad))

    (::count/specs bad)

    ;; Could the rest be errors/disabled? It's got to sum to the specs or cases SOMEhow right?


    (count (suite/cases bad))


    (::count/specs bad)

    (::count/failures bad)

    (count (failure/failures bad))

    (::count/errors bad)
    (::count/disabled bad)

    (::run/date bad)


    "Exploring failures"

    (pprint (suite/expectations (first run/runs)))

    (pprint (first (map #(dissoc % :cyclotron.failure/stacktrace) (failure/failures (first run/runs)))))

    ;; Ok. The first run is.... very bad. But it's not as bad as it says! Right?

    (pprint (dissoc (first run/runs) :cyclotron.run/report))

    (pprint (first (map #(dissoc % :cyclotron.failure/stacktrace) (failure/failures (first run/runs)))))

    ;; Succeeds, 111 specs: https://gitlab.logicgate.com/platform/logicgate/pipelines/14454
    ;; Succeeds, nodes suite, 202 specs: https://gitlab.logicgate.com/platform/logicgate/pipelines/14898

    (def root "/home/man/logicgate/dev/logicgate/platform/client/")

    (alias 'fail 'cyclotron.failure)

    ::fail/specs

    (def example-failure '#:cyclotron.failure{:message
                                              "TimeoutError: Wait timed out after 30000ms",
                                              :specs
                                              (#:cyclotron.failure{:file
                                                                   "e2e/specs/app/build/processes/node/build-node.spec.ts",
                                                                   :line "22",
                                                                   :char "3)"}
                                                                  #:cyclotron.failure{:file
                                                                                      "e2e/specs/app/build/processes/node/build-node.spec.ts",
                                                                                      :line "12",
                                                                                      :char "1)"}),
                                              :pages
                                              (#:cyclotron.failure{:file
                                                                   "e2e/pages/common/panel.ts",
                                                                   :line "26",
                                                                   :char "20)"}
                                                                  #:cyclotron.failure{:file
                                                                                      "e2e/pages/common/page.ts",
                                                                                      :line "34",
                                                                                      :char "24"})})

    (string/split-lines (slurp (str root (::fail/file (first (example-failure ::fail/specs))))))

    (def root "/home/man/logicgate/dev/logicgate/platform/client/")

    (defn spec-coordinates [failure]
      (->> (::fail/specs failure)
           (map #(dissoc % ::fail/char))
           (map #(update % ::fail/line str->int))))



    (let [coords (first (spec-coordinates example-failure))
          content (slurp (str root (::fail/file coords)))
          line (::fail/line coords)]
      line
      ))

;; (defn run-stats [file]
;;   (let [xml (run-data file)]
;;     (-> (:attrs xml)
;;         (update :disabled str->int)
;;         (update :errors str->int)
;;         (update :failures str->int)
;;         (update :tests str->int)
;;         (update :time str->float)
;;         (set/rename-keys {:disabled :cyclotron.run.count/disabled
;;                           :errors   :cyclotron.run.count/errors
;;                           :failures :cyclotron.run.count/failures
;;                           :tests    :cyclotron.run.count/specs
;;                           :time     :cyclotron.run/elapsed-time}))))

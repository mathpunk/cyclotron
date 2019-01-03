(ns user)

(do "Preamble"

    (require '[clojure.spec.alpha :as s]
             '[clojure.spec.test.alpha :as t]
             '[clojure.spec.gen.alpha :as g]
             '[clojure.string :as string]
             '[clojure.java.io :as io]
             '[clojure.pprint :refer [print-table pprint]]
             '[cognitect.transcriptor :refer [check!]])

    (set! *print-length* 10)

    (set! *print-level* 6)

    #_(do
      (require '[cyclotron.cache :as cache])
      (cache/init))

    )

(do "Require all the things!"
  (require '[cyclotron.run :as run])
  (require '[cyclotron.suite :as suite])
  (require '[cyclotron.failure :as failure])
  (require '[cyclotron.utils :refer [str->int str->float]]))


(do "Exploring failures"

    (pprint (suite/expectations (first run/runs)))

    (pprint (first (map #(dissoc % :cyclotron.failure/stacktrace) (failure/failures (first run/runs)))))

    ;; Ok. The first run is.... very bad. But it's not as bad as it says! Right?

    (pprint (dissoc (first run/runs) :cyclotron.run/data))

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

    (defn spec-coordinates [failure]
      (->> (::fail/specs failure)
           (map #(dissoc % ::fail/char))
           (map #(update % ::fail/line str->int))))



    (let [coords (first (spec-coordinates example-failure))
          content (slurp (str root (::fail/file coords)))
          line (::fail/line coords)]
      line
      ))

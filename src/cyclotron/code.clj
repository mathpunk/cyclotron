(ns cyclotron.code
  (:refer-clojure :exclude [hash parents])
  (:require [clj-jgit.porcelain :as git]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clj-jgit.querying :refer [find-rev-commit changed-files-between-commits]]
            [clj-jgit.internal :as git-internal]
            [java-time :as time]))

;; Datafying a repository
;; ================================================
;; TODO: Clone?

(def local-repo ;; TODO: Should be from a config option
  (let [base "/home/man/logicgate/dev/logicgate"]
    (git/load-repo base)))

(def repo local-repo)

(def rev-walk (git-internal/new-rev-walk repo))


;; Commits
;; =================================
(defn get-rev
  "A RevCommit is a Java object representing a commit"
  [sha]
  (find-rev-commit repo rev-walk sha))

(def log
  "A reverse-chron sequence of RevCommits. Starts from 'where we are'"
  (partial git/git-log repo))

(def head (first (log)))

(s/def ::rev-commit #(= org.eclipse.jgit.revwalk.RevCommit (type %)))

(s/def ::name string?)
(s/def ::email string?)
(s/def ::developer (s/keys :req [::name ::email]))
(s/def ::author ::developer)
(s/def ::committer ::developer)

(s/def ::commit (s/keys :req [::hash ::author ::committer ::time ::message ::full-message]))

(s/def ::hashable (s/or :map ::commit
                        :object ::rev-commit))

(defmulti hash (fn [x] (first (s/conform ::hashable x))))

(defmethod hash :map
  [x]
  (::hash x))

(defmethod hash :object
  [x]
  (.getName x ))

(defn ->commit [revcommit]
  (let [stringified (.toString revcommit)
        author (.getAuthorIdent revcommit)
        committer (.getCommitterIdent revcommit)
        epoch-in-seconds (.getCommitTime revcommit)
        message (.getShortMessage revcommit)
        full-message (.getFullMessage revcommit)
        parent-count (.getParentCount revcommit)]
    {::hash (second (string/split stringified #" "))
     ::author {::name (.getName author) ::email (.getEmailAddress author)}
     ::committer {::name (.getName committer) ::email (.getEmailAddress committer)}
     ::time (time/instant (* 1000 epoch-in-seconds))
     ::message message
     ::full-message full-message
     ::parents (if (> parent-count 1)
                 (throw (Exception. (str "There's more than one parent for the commit at hash " (hash revcommit)". That's going to cause problems.")))
                 (first (.getParent revcommit 0)))}))

(defn parents [commit]
  (map ->commit (::parents commit)))


;; Branches
;; =================
(def checkout (partial git/git-checkout repo))

(def branch-create (partial git/git-branch-create repo))

(defn branch-there-and-checkout
  [hash title]
  (do
    (checkout hash)
    (branch-create title)
    (checkout title)))



;; Multiple commits: chains, transitions, differences...
;; ========================================================

(defn scout-to
  "A sequence of commits from HEAD to the given sha. (Why 'scout'? Because we don't actually
  change HEAD, we just get data.)"
  [sha]
  (loop [head (->commit head)
         acc []]
    (if (= sha (hash head))
      (conj acc head)
      (recur (first (parents head)) (conj acc head)))))

(defn transition-from
  "Given a sha, returns a pair of commits. The first is the commit for the given sha, and the
  second is for the one immediately after."
  [sha]
  (take 2 (reverse (scout-to sha))))

(defn changed-files
  "Given a pair of shas, returns a sequence of the filenames that were changed between the two. (This fn drops the key that tells us WHAT happened, because it's almost always an :edit. /shrug)"
  [sha1 sha2] 
  (let [revs (map get-rev [sha1 sha2])]
    (apply (partial changed-files-between-commits repo) revs)))

(defn changed-files-immediately-after
  [sha1]
  (let [transition (transition-from sha1)
        hashes (map hash transition)]
    (map first (apply changed-files hashes))))

(do "Blame. TODO: Figure out how what info you really want, rather than 'everyone
who has done anything to this file'"

    (def blame (partial git/git-blame repo))

    (first (blame "platform/client/e2e"))
    )


(comment "Evidence we mostly don't care about commits with multiple parents. There's that
`throw` for safety tho."

         (defn single-parented? [commit]
           (= 1 (.getParentCount commit)))

         ;; Starting from latest, 
         (count (take-while single-parented? (log)))
         ;; ...1560 in a row. Fuhgeddaboutit!
         )

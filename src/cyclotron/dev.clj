(ns cyclotron.dev
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as t]
            [clojure.spec.gen.alpha :as g]
            [clojure.string :as str]
            [clojure.pprint :refer [print-table pprint]]
            [cognitect.transcriptor :refer [check!]]))

(set! *print-length* 4)



(do

  )




;;;;;;;;;;;;;;;;;;;;WIP/REFERENCE/TRASH;;;;;;;;;;;;

(comment "Cloning. Would require authentication so, punting and using my local."

         ;; Clone a repository into a folder of my choosing and keep the jgit repository object at my-repo
         (def remote-repo
           (:repo (git/git-clone-full "https://github.com/clj-jgit/clj-jgit.git" "local-folder/clj-jgit"))))

(comment "Load repo code that I probably don't need"

         (def local-repo
           (let [base "/home/man/logicgate/dev/logicgate"]
             (git/load-repo base)))

         (def repo local-repo)



         ;; Repos
         ;; ========================
         (require '[clj-jgit.porcelain :as git]
                  '[clojure.java.io :as io]))

(comment "Working with blames for files"

         
         ;; Files
         ;; ===================
         (def some-filename ".gitignore")
         (.isFile (io/as-file (str base "/" some-filename)))

         (first (git/git-blame repo some-filename))

         (comment
           {:author {:name "Mike Blum",
                     :email "mike.blum@logicgate.com",
                     :timezone #object[sun.util.calendar.ZoneInfo 0x4510e3c "sun.util.calendar.ZoneInfo[id=\"GMT-06:00\",offset=-21600000,dstSavings=0,useDaylight=false,transitions=0,lastRule=null]"]},

            :commit #object[org.eclipse.jgit.revwalk.RevCommit 0x728b29c9 "commit b89631deafe7024aa640d5cc5f5d8427737b396e 1520642036 -----p"],
            :committer {:name "Mike Blum",
                        :email "mike.blum@logicgate.com",
                        :timezone #object[sun.util.calendar.ZoneInfo 0x27298cbd "sun.util.calendar.ZoneInfo[id=\"GMT-06:00\",offset=-21600000,dstSavings=0,useDaylight=false,transitions=0,lastRule=null]"]},
            :line 0,
            :line-contents "### Java",
            :source-path ".gitignore"})



         ;; Logs
         ;; ==============
         (type (first (git/git-log repo)))
         (check! #{org.eclipse.jgit.revwalk.RevCommit}))

(comment "Stuff involving changed files between commits"

         ;; Finding changes
         (require '[clj-jgit.querying :refer [find-rev-commit changed-files-between-commits]])

         ;; It takes a pair of RevCommits as its args. How do I make those?

         ;; (find-rev-commit repo "e4a536c5c")
         ;; No, because you need a RevWalk, too. git-log? No, new rev walk, from internal (why tho)

         (require '[clj-jgit.internal :refer [new-rev-walk]])


         (def rev-walk (new-rev-walk repo))

         (find-rev-commit repo rev-walk "e4a536c5c")
         ;; => RevCommit

         (def one-hashish "e6722732f")

         (def another-hashish "e4a536c5c")

         (let [walk (new-rev-walk repo)
               commit-1 (find-rev-commit repo walk one-hashish)
               commit-2 (find-rev-commit repo walk another-hashish)]
           (changed-files-between-commits repo commit-1 commit-2))

         ;; Woot!

         )


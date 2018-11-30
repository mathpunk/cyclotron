(ns cyclotron.state)


;; (require '[clojure.java.io :as io])

;; (def frozen-db (nth (file-seq (io/as-file (io/resource "state"))) 1))

;; (type frozen-db)

;; (require '[clojure.java.shell :refer [sh]])



;; neo4j stop
;; neo4j-admin restore --from=test.logicgate.com/ --database=graph.db --force
;; neo4j start
;; tail -f /var/log/neo4j/neo4j.log


;;  cat retrieve-and-load-db.sh 
;; #!/usr/bin/env bash

;; ENVIRONMENT=${1:?"environment argument required"}
;; TIER=${2:?"tier argument required"}
;; REGION=${3:?"region argument required"}
;; DATABASE=${4:?"database argument required"}

;; BUCKET=logicgate-neo4j-backups

;; if [ $REGION = "eu-west-1" ]; then
;;   BUCKET=logicgate-neo4j-backups-eu
;; fi

;; PREFIX=$TIER-logicgate-neo4j-$ENVIRONMENT

;; YEAR=`date +%Y`
;; MONTH=`date +%m`
;; DAY=`date +%d`
;; DATE_PATH=$YEAR/$MONTH/$DAY

;; KEY=$PREFIX/$DATE_PATH/neo4j-backup-$TIER-logicgate-neo4j-$ENVIRONMENT.tar.gz

;; DEST=/tmp/backups/backup.tar.gz
;; MNT=/tmp/backups/mnt
;; BACKUP=$MNT/backups/neo4j/$DATE_PATH/$PREFIX-graph.db.backup

;; sudo mkdir -p /tmp/backups
;; sudo chown aws:aws -R /tmp/backups
;; sudo -H -u aws bash -c "aws s3 cp s3://${BUCKET}/${KEY} ${DEST}"
;; sudo -H -u aws bash -c "tar -xzf ${DEST} -C /tmp/backups"

;; sudo mkdir -p /var/run/neo4j
;; sudo chown neo4j:neo4j /var/run/neo4j
;; sudo chown neo4j:neo4j -R /tmp/backups
;; sudo -H -u neo4j bash -c "sudo systemctl stop neo4j"
;; sudo -H -u neo4j bash -c "/opt/neo4j/bin/neo4j-admin restore --from=${BACKUP} --database=${DATABASE} --force"
;; sudo -H -u neo4j bash -c "sudo systemctl start neo4j"

;; sudo -H -u neo4j bash -c "rm ${DEST}"
;; sudo -H -u neo4j bash -c "rm -rf ${MNT}"

;; tail -f /var/log/neo4j/neo4j.log
;; [I] 





;; cat load-db.sh 
;; #!/usr/bin/env bash
;; neo4j stop
;; neo4j-admin restore --from=test.logicgate.com/ --database=graph.db --force
;; neo4j start
;; tail -f /var/log/neo4j/neo4j.log

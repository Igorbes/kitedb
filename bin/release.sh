#!/usr/bin/env bash
set -e

echo 'Release KiteDB'
cd ..
mvn clean
#mvn versions:update-properties -DallowSnapshots=false
#git diff-index --quiet HEAD || git commit -m "Update dependency of modules" -a
mvn -B release:clean release:prepare -Dgoals=clean -DpreparationGoals=compile -DpushChanges=false -DskipTests=true -DignoreSnapshots=true
git push https://github.com/Igorbes/kitedb.git refs/heads/master:refs/heads/master --tags
git push
echo "Complete release KiteDB"
exit 0
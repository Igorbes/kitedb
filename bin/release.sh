#!/usr/bin/env bash
set -e

echo 'Release KiteDB'
cd ..
mvn clean
#mvn versions:update-properties -DallowSnapshots=false
#git diff-index --quiet HEAD || git commit -m "Update dependency of modules" -a
mvn -B release:clean release:prepare -Dgoals=clean -DpreparationGoals=compile -DpushChanges=false -DskipTests=true -DignoreSnapshots=true
echo "Complete release KiteDB"
exit 0
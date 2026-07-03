#!/usr/bin/env bash
set -e

echo 'Deploy latest db-graph'
cd ..
TAG="$(git describe --abbrev=0 --tags)"
echo $TAG
git checkout tags/$TAG
mvn -Pdevelop deploy
git checkout master
echo "Complete deploy db-graph $JAR"
exit 0
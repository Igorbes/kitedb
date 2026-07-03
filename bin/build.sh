#!/usr/bin/env bash
set -e

echo 'Build latest KiteDB'
cd ..
TAG="$(git describe --abbrev=0 --tags)"
echo $TAG
git checkout tags/$TAG
mvn package
git checkout master

cd target
JAR="$(find *.jar)"
echo $JAR
##scp -i F:/keys/selectel-production.pem -vvv  $JAR centos@46.148.230.34:/opt/cherokeesoft/save-service/

echo "Complete build KiteDB $JAR"
exit 0
#!/usr/bin/env bash

echo "appcenter per build"

cd app
mkdir cert
cd cert
echo $ANDROID_KEY_PROPERTIES > key.properties.base64
base64 --decode key.properties.base64 > key.properties

echo $ANDROID_UPLOAD_JKS > RPLink.jks.base64
base64 --decode RPLink.jks.base64 > RPLink.jks

echo "java -version"
java -version

cd ..
cd ..
pwd
ls -la

echo "gradlew test"
bash gradlew

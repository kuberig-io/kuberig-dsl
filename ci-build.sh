source ci-gradle-init.sh
./gradlew assemble \
  -Pgradle.publish.key=${GRADLE_PUBLISH_KEY}

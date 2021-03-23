source ci-gradle-init.sh
gradle \
  -Pgradle.publish.key="${GRADLE_PUBLISH_KEY}" \
  -Pgradle.publish.secret="${GRADLE_PUBLISH_SECRET}" \
  -PsonatypeUsername="${SONATYPE_USERNAME}" \
  -PsonatypePassword="${SONATYPE_PASSWORD}" \
  -Psigning.keyId="${SIGNING_KEY_ID}" \
  -Psigning.password="${SIGNING_PASSWORD}" \
  -Psigning.secretKeyRingFile="${PLAIN_M2_SIGNING_KEY}" \
  "$@"
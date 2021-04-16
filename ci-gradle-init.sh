export GRADLE_USER_HOME=/builds/kuberig/.gradle
echo $GRADLE_USER_HOME
mkdir -p $GRADLE_USER_HOME
export PLAIN_M2_SIGNING_KEY=${GRADLE_USER_HOME}/m2_signing_key.pgp
rm -rf $PLAIN_M2_SIGNING_KEY
echo $M2_SIGNING_KEY | base64 -d > ${PLAIN_M2_SIGNING_KEY}
echo $GRADLE_USER_HOME_PROPERTIES > $GRADLE_USER_HOME/.gradle.properties
if [ -z "$CI_COMMIT_TAG" ]; then git checkout -B $CI_COMMIT_REF_NAME $CI_COMMIT_SHA; fi
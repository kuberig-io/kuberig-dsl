export GRADLE_USER_HOME=/builds/kuberig/.gradle
echo $GRADLE_USER_HOME
mkdir -p $GRADLE_USER_HOME
echo $M2_SIGNING_KEY | base64 -d > /builds/kuberig/teyckmans@gmail.com.pgp
echo $GRADLE_USER_HOME_PROPERTIES > $GRADLE_USER_HOME/.gradle.properties
if [ -z "$CI_COMMIT_TAG" ]; then git checkout -B $CI_COMMIT_REF_NAME $CI_COMMIT_SHA; fi
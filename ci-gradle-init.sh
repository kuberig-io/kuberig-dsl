export GRADLE_USER_HOME=`pwd`/.gradle
mkdir -p $GRADLE_USER_HOME
export PLAIN_M2_SIGNING_KEY=${GRADLE_USER_HOME}/m2_signing_key.pgp
rm -rf $PLAIN_M2_SIGNING_KEY
echo $M2_SIGNING_KEY | base64 -d > ${PLAIN_M2_SIGNING_KEY}
export GRADLE_OPTS='-Dorg.gradle.daemon=false -Dorg.gradle.caching=true -Dorg.gradle.parallel=true -Dorg.gradle.jvmargs="-Xmx4g -XX:MaxMetaspaceSize=1g"'
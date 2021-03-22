export GRADLE_USER_HOME=`pwd`/.gradle
mkdir -p $GRADLE_USER_HOME
export PLAIN_M2_SIGNING_KEY=${GRADLE_USER_HOME}/m2_signing_key.pgp
rm -rf $PLAIN_M2_SIGNING_KEY
echo $M2_SIGNING_KEY | base64 -d > ${PLAIN_M2_SIGNING_KEY}
export GRADLE_PROPS_FILE=$GRADLE_USER_HOME/gradle.properties
rm -rf $GRADLE_PROPS_FILE
touch $GRADLE_PROPS_FILE
echo "gradle.publish.key=${GRADLE_PUBLISH_KEY}" >> $GRADLE_PROPS_FILE
echo "gradle.publish.secret=${GRADLE_PUBLISH_SECRET}" >> $GRADLE_PROPS_FILE
echo "signing.keyId=${SIGNING_KEY_ID}" >> $GRADLE_PROPS_FILE
echo "signing.password=${SIGNING_PASSWORD}" >> $GRADLE_PROPS_FILE
echo "signing.secretKeyRingFile=${PLAIN_M2_SIGNING_KEY}" >> $GRADLE_PROPS_FILE
echo "mavenCentralUsername=${SONATYPE_USERNAME}" >> $GRADLE_PROPS_FILE
echo "mavenCentralPassword=${SONATYPE_PASSWORD}" >> $GRADLE_PROPS_FILE
export GRADLE_OPTS='-Dorg.gradle.daemon=false -Dorg.gradle.caching=true -Dorg.gradle.parallel=true'
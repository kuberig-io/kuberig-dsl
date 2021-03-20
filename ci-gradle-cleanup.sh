export GRADLE_USER_HOME=`pwd`/.gradle
ls $GRADLE_USER_HOME
export GRADLE_PROPS_FILE=$GRADLE_USER_HOME/gradle.properties
if test -f "$GRADLE_PROPS_FILE"; then
  rm -rf $GRADLE_PROPS_FILE
  echo "gradle.properties file cleaned up"
else
  echo "no gradle.properties file found"
fi
export PLAIN_M2_SIGNING_KEY=${GRADLE_USER_HOME}/m2_signing_key.pgp
if test -f "$PLAIN_M2_SIGNING_KEY"; then
  rm -rf $PLAIN_M2_SIGNING_KEY
  echo "PGP signing key file cleaned up"
else
  echo "No PGP signing key file found"
fi
ls $GRADLE_USER_HOME
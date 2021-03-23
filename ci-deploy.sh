source ci-gradle-init.sh
sleep 600
./gradlew deploy -x test --stacktrace

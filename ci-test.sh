source ci-gradle-init.sh
./gradlew check
bash <(curl -s https://codecov.io/bash)


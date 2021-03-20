#!/bin/bash
set -o allexport
source .env
set +o allexport

export CI=true
export CI_COMMIT_TAG=0.1.7-RC16

./gradle deploy -x test --stacktrace
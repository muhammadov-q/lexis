#!/usr/bin/env bash

set -euo pipefail

./gradlew :shared:allTests :androidApp:assembleDebug

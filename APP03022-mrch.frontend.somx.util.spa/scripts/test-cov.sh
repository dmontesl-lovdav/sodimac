#!/usr/bin/env bash

set -euo pipefail

jest --coverage --passWithNoTests "$@"

if [ ! -f coverage/lcov.info ]; then
  mkdir -p coverage
  : > coverage/lcov.info
fi
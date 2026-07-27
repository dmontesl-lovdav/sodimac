#!/usr/bin/env bash

set -euo pipefail

npx jest --config jest.config.cjs --coverage "$@"

test -s coverage/lcov.info
#!/usr/bin/env bash

set -euo pipefail

rm -rf coverage

npx jest \
  --config jest.config.js \
  --coverage \
  --passWithNoTests \
  "$@"

if [ ! -s coverage/lcov.info ]; then
  echo "ERROR: Jest no generó un reporte válido en coverage/lcov.info."
  exit 1
fi

echo "Reporte de cobertura generado correctamente:"
echo "coverage/lcov.info"

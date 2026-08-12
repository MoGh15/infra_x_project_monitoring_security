#!/bin/sh
set -e

API_URL="${API_URL:-http://localhost:8080}"

# generate env.js
sed "s|\$API_URL|$API_URL|g" \
  /usr/share/nginx/html/assets/env.template.js \
  > /usr/share/nginx/html/assets/env.js

exec "$@"

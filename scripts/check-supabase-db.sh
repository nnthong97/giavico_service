#!/usr/bin/env zsh
set -euo pipefail

env_file="${1:-.env.supabase.local}"

if [[ ! -f "$env_file" ]]; then
  print -u2 "Missing env file: $env_file"
  exit 1
fi

set -a
source "$env_file"
set +a

if [[ -z "${SPRING_DATASOURCE_URL:-}" || -z "${SPRING_DATASOURCE_USERNAME:-}" || -z "${SPRING_DATASOURCE_PASSWORD:-}" ]]; then
  print -u2 "SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, and SPRING_DATASOURCE_PASSWORD are required."
  exit 1
fi

jdbc_no_prefix="${SPRING_DATASOURCE_URL#jdbc:postgresql://}"
db_host="${jdbc_no_prefix%%:*}"
rest="${jdbc_no_prefix#*:}"
db_port="${rest%%/*}"
db_name_with_query="${rest#*/}"
db_name="${db_name_with_query%%\?*}"

print "Testing Supabase Postgres connection..."
print "host=$db_host port=$db_port dbname=$db_name user=$SPRING_DATASOURCE_USERNAME"

PGPASSWORD="$SPRING_DATASOURCE_PASSWORD" psql \
  "host=$db_host port=$db_port dbname=$db_name user=$SPRING_DATASOURCE_USERNAME sslmode=require connect_timeout=10" \
  -c "select current_user, current_database();"

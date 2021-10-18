#!/usr/bin/env bash

set -Eeuo pipefail

BASE_DIR=$(dirname "${BASH_SOURCE[0]:-$0}")
cd "${BASE_DIR}/../../.." || exit 127

# shellcheck source=../../../scripts/helpers.sh
. scripts/helpers.sh

if not_installed "http"; then
  log_error "You must download httpie first..." "See httpie.org for more information"
fi

function request() {
  METHOD=${1:-GET}
  ENDPOINT=${2}
  shift 2
  http --check-status --ignore-stdin --timeout=2.5 -v "${METHOD}" ":${APP_SERVER_PORT:-9090}/api/v1${ENDPOINT}" "$@"
}

log_info "POST" "Create Portalegre district"
request POST /districts name="portalegre" contacted:=5 infected:=15 users:=200

log_info "POST" "Create Porto district"
request POST /districts name="porto" contacted:=2 infected:=15 users:=10

log_info "POST" "Create Lisboa district"
request POST /districts name="lisboa" users:=60 infected:=30 contacted:=3

log_info "GET" "Fetch Portalegre information"
request GET /districts/portalegre

log_info "GET" "Fetch districts"
request GET /districts

log_info "DELETE" "Remove Porto district"
request DELETE /districts/porto

log_info "POST" "Create Porto district"
request POST /districts name="porto"

log_info "PUT" "Update Porto district"
request PUT /districts/porto name="porto" contacted:=5 users:=60 infected:=55

log_info "GET" "Sort districts by ratio"
request GET /districts?sort=ratio

log_info "GET" "Sort districts by users"
request GET /districts?sort=users

log_info "POST" "Create locations (0,0) for Braga"
request POST /locations district="Braga" x:=0 y:=0

log_info "POST" "Create locations (0,0) for Lisboa"
request POST /locations district="Lisboa" x:=0 y:=0

log_info "POST" "Create locations (0,0) for Porto"
request POST /locations district="porto" x:=0 y:=0 crowding:=5

log_info "PUT" "Update locations (0,0) for Braga"
request PUT /locations/-1968155392 district="Braga" x:=0 y:=0 crowding:=200

log_info "GET" "Sort locations by crowding"
request GET /locations?sort=crowding

log_info "GET" "Sort locations by district"
request GET /locations?sort=district\&limit=500

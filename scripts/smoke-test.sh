#!/usr/bin/env bash
# Black-box smoke test of a running PropertyHub. Usage: smoke-test.sh <base-url> [expected-git-sha]
set -euo pipefail
BASE="${1%/}"
EXPECTED_SHA="${2:-}"

retry() { for _ in $(seq 1 30); do "$@" && return 0; sleep 5; done; return 1; }

echo "Health..."
retry bash -c "curl -fsS '$BASE/actuator/health' | grep -q UP"

if [ -n "$EXPECTED_SHA" ]; then
  echo "Version (expecting ${EXPECTED_SHA})..."
  retry bash -c "curl -fsS '$BASE/actuator/info' | grep -q '$EXPECTED_SHA'"
fi

echo "Home page..."
curl -fsS "$BASE/" | grep -q "Best value right now"

echo "Assignment banner..."
curl -fsS "$BASE/about" | grep -q "L00205200"

echo "Search API..."
COUNT=$(curl -fsS "$BASE/api/listings?type=SALE" | jq 'length')
[ "$COUNT" -gt 0 ]

echo "Valuation API..."
EST=$(curl -fsS -X POST "$BASE/api/valuation" -H 'Content-Type: application/json' \
  -d '{"county":"DUBLIN","propertyType":"SEMI_DETACHED","beds":3,"floorAreaSqm":110,"ber":"B2","newBuild":false}' \
  | jq '.estimate')
[ "$EST" -gt 0 ]

echo "Security headers..."
curl -fsSI "$BASE/" | grep -qi "content-security-policy"

echo "Smoke test passed: ${COUNT} listings, sample valuation €${EST}"

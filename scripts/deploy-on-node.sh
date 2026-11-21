#!/usr/bin/env bash
# Runs ON the k3s node (invoked by the pipeline through AWS SSM Run Command).
# Usage: deploy-on-node.sh <image-repository> <image-tag> <git-sha>
# 1. waits for the cluster bootstrap to finish
# 2. helm upgrade --atomic (automatic rollback if pods never become ready)
# 3. post-deploy gate: Prometheus must scrape the new pods and no critical alert may be firing,
#    otherwise the release is rolled back and the pipeline fails (monitoring -> pipeline feedback loop)
set -euo pipefail

IMAGE_REPO="$1"
IMAGE_TAG="$2"
GIT_SHA="$3"
NS=propertyhub
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
export PATH=$PATH:/usr/local/bin

echo "==> Waiting for node bootstrap (k3s + monitoring stack)"
for _ in $(seq 1 120); do
  [ -f /opt/propertyhub-bootstrap-complete ] && break
  sleep 10
done
[ -f /opt/propertyhub-bootstrap-complete ] || { echo "Bootstrap did not complete"; tail -50 /var/log/propertyhub-bootstrap.log; exit 1; }

PREVIOUS_REVISION=$(helm -n "$NS" history propertyhub --max 1 -o json 2>/dev/null | jq -r '.[0].revision // empty' || true)

echo "==> helm upgrade ${IMAGE_REPO}:${IMAGE_TAG}"
helm upgrade --install propertyhub ./helm/propertyhub \
  --namespace "$NS" --create-namespace \
  --set image.repository="$IMAGE_REPO" \
  --set image.tag="$IMAGE_TAG" \
  --set gitCommit="$GIT_SHA" \
  --atomic --wait --timeout 6m --history-max 10

kubectl -n "$NS" rollout status deploy/propertyhub --timeout=180s
kubectl -n "$NS" get pods -o wide

echo "==> Post-deploy gate: checking Prometheus"
PROM_IP=$(kubectl -n monitoring get svc kube-prometheus-stack-prometheus -o jsonpath='{.spec.clusterIP}')
query() { curl -fsS --get "http://${PROM_IP}:9090/api/v1/query" --data-urlencode "query=$1" | jq -r '.data.result[0].value[1] // "0"'; }

rollback() {
  echo "!! $1 - rolling back"
  if [ -n "$PREVIOUS_REVISION" ]; then
    helm -n "$NS" rollback propertyhub "$PREVIOUS_REVISION" --wait --timeout 5m
  fi
  exit 1
}

UP=0
for _ in $(seq 1 12); do
  UP=$(query 'sum(up{job="propertyhub"})')
  [ "${UP%.*}" -ge 1 ] 2>/dev/null && break
  sleep 10
done
[ "${UP%.*}" -ge 1 ] 2>/dev/null || rollback "Prometheus is not scraping any healthy PropertyHub pod"
echo "Prometheus sees ${UP} healthy target(s)"

# Give new pods time to serve traffic, then make sure nothing critical is firing
sleep 30
FIRING=$(query 'count(ALERTS{alertstate="firing",app="propertyhub",severity="critical"})')
[ "${FIRING%.*}" -eq 0 ] 2>/dev/null || rollback "${FIRING} critical alert(s) firing after deploy"

echo "==> Deploy of ${IMAGE_TAG} healthy"

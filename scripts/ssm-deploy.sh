#!/usr/bin/env bash
# Runs in GitHub Actions. Sends the deploy to the EC2 node via AWS SSM Run Command (no SSH, no open ports)
# and streams back the result. Requires AWS credentials (OIDC) and:
#   INSTANCE_ID, AWS_REGION, IMAGE_REPO, IMAGE_TAG, GIT_SHA, REPO_URL
set -euo pipefail

: "${INSTANCE_ID:?}" "${AWS_REGION:?}" "${IMAGE_REPO:?}" "${IMAGE_TAG:?}" "${GIT_SHA:?}" "${REPO_URL:?}"

PARAMS=$(jq -n --arg repo "$REPO_URL" --arg sha "$GIT_SHA" --arg img "$IMAGE_REPO" --arg tag "$IMAGE_TAG" '{
  commands: [
    "set -e",
    "rm -rf /opt/propertyhub-src && mkdir -p /opt/propertyhub-src && cd /opt/propertyhub-src",
    "git init -q && git remote add origin \($repo)",
    "git fetch -q --depth 1 origin \($sha) && git checkout -q FETCH_HEAD",
    "bash scripts/deploy-on-node.sh \($img) \($tag) \($sha)"
  ],
  executionTimeout: ["1800"]
}')

echo "Sending deploy command to ${INSTANCE_ID}"
COMMAND_ID=$(aws ssm send-command \
  --region "$AWS_REGION" \
  --instance-ids "$INSTANCE_ID" \
  --document-name AWS-RunShellScript \
  --comment "Deploy ${IMAGE_TAG} (${GITHUB_RUN_ID:-local})" \
  --timeout-seconds 600 \
  --parameters "$PARAMS" \
  --query Command.CommandId --output text)
echo "SSM command: ${COMMAND_ID}"

STATUS=Pending
for _ in $(seq 1 180); do
  sleep 10
  STATUS=$(aws ssm get-command-invocation --region "$AWS_REGION" --command-id "$COMMAND_ID" \
    --instance-id "$INSTANCE_ID" --query Status --output text 2>/dev/null || echo Pending)
  case "$STATUS" in
    Pending|InProgress|Delayed) echo "  status: ${STATUS}" ;;
    *) break ;;
  esac
done

aws ssm get-command-invocation --region "$AWS_REGION" --command-id "$COMMAND_ID" \
  --instance-id "$INSTANCE_ID" --query '[StandardOutputContent, StandardErrorContent]' --output text || true

echo "Final status: ${STATUS}"
[ "$STATUS" = "Success" ]

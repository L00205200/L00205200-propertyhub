#!/usr/bin/env bash
# Create or update the CloudFormation stack, wait for the node bootstrap, print outputs.
# Usage: scripts/cfn-deploy.sh [personal|learner-lab] [stack-name] [region]
set -euo pipefail
PROFILE_KIND="${1:-personal}"
STACK="${2:-propertyhub}"
REGION="${3:-${AWS_REGION:-us-east-1}}"
DIR="$(cd "$(dirname "$0")/../infra/cloudformation" && pwd)"
PARAMS="params-${PROFILE_KIND}.json"
# Work from the template folder with relative paths (avoids Windows/Git Bash path conversion issues)
cd "$DIR"

echo "==> Validating template"
aws cloudformation validate-template --region "$REGION" \
  --template-body file://propertyhub.yaml >/dev/null

# Convert params JSON to Key=Value pairs for 'deploy' (supports both create and update)
mapfile -t OVERRIDES < <(jq -r '.[] | "\(.ParameterKey)=\(.ParameterValue)"' "$PARAMS")

echo "==> Deploying stack ${STACK} in ${REGION} (${PROFILE_KIND}); the node bootstrap takes 8-15 minutes"
aws cloudformation deploy --region "$REGION" \
  --stack-name "$STACK" \
  --template-file propertyhub.yaml \
  --capabilities CAPABILITY_NAMED_IAM \
  --parameter-overrides "${OVERRIDES[@]}" \
  --tags Project=propertyhub ManagedBy=cloudformation \
  --no-fail-on-empty-changeset

aws cloudformation describe-stacks --region "$REGION" --stack-name "$STACK" \
  --query 'Stacks[0].Outputs[].[OutputKey,OutputValue]' --output table

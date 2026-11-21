#!/usr/bin/env bash
# Copy CloudFormation stack outputs into GitHub repository variables (requires: gh auth login).
# Usage: scripts/set-github-vars-cfn.sh [stack-name] [region]
set -euo pipefail
STACK="${1:-propertyhub}"
REGION="${2:-${AWS_REGION:-us-east-1}}"

out() {
  aws cloudformation describe-stacks --region "$REGION" --stack-name "$STACK" \
    --query "Stacks[0].Outputs[?OutputKey=='$1'].OutputValue" --output text
}

gh variable set AWS_REGION      --body "$(out AwsRegion)"
gh variable set EC2_INSTANCE_ID --body "$(out InstanceId)"
gh variable set APP_URL         --body "$(out AppUrl)"
gh variable set GRAFANA_URL     --body "$(out GrafanaUrl)"

ROLE="$(out DeployRoleArn)"
if [ -n "$ROLE" ] && [ "$ROLE" != "None" ]; then
  gh variable set AWS_DEPLOY_ROLE_ARN --body "$ROLE"
else
  echo "No deploy role (Learner Lab mode). Add the lab's temporary credentials as GitHub *secrets*:"
  echo "  AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_SESSION_TOKEN  (AWS Details > AWS CLI in the lab; they expire every lab session)"
fi
gh variable list

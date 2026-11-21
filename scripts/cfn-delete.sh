#!/usr/bin/env bash
# Delete the stack (and everything in it) to stop charges. Usage: scripts/cfn-delete.sh [stack-name] [region]
set -euo pipefail
STACK="${1:-propertyhub}"
REGION="${2:-${AWS_REGION:-us-east-1}}"
aws cloudformation delete-stack --region "$REGION" --stack-name "$STACK"
echo "Deleting ${STACK}..."
aws cloudformation wait stack-delete-complete --region "$REGION" --stack-name "$STACK"
echo "Deleted. Note: the Grafana secret is scheduled for deletion (7-30 day recovery window)."

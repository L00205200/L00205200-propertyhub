#!/usr/bin/env bash
# Copy Terraform outputs into GitHub repository variables (not secrets: none of these are sensitive).
# Requires the GitHub CLI logged in (gh auth login). Run from the repo root after terraform apply.
set -euo pipefail
cd "$(dirname "$0")/../infra/terraform"

out() { terraform output -raw "$1"; }

gh variable set AWS_REGION            --body "$(out aws_region)"
gh variable set EC2_INSTANCE_ID       --body "$(out instance_id)"
gh variable set APP_URL               --body "$(out app_url)"
gh variable set GRAFANA_URL           --body "$(out grafana_url)"
gh variable set AWS_DEPLOY_ROLE_ARN   --body "$(out deploy_role_arn)"
gh variable set AWS_TERRAFORM_ROLE_ARN --body "$(out terraform_role_arn)"
if [ -f backend.hcl ]; then
  gh variable set TF_STATE_BUCKET --body "$(grep bucket backend.hcl | cut -d'"' -f2)"
fi
gh variable list

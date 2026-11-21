#!/usr/bin/env bash
# One-off: create an encrypted, versioned, private S3 bucket for Terraform state and write backend.hcl.
# Usage: scripts/bootstrap-tf-state.sh [region]
set -euo pipefail
REGION="${1:-eu-west-1}"
ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
BUCKET="propertyhub-tfstate-${ACCOUNT}"

if ! aws s3api head-bucket --bucket "$BUCKET" 2>/dev/null; then
  aws s3api create-bucket --bucket "$BUCKET" --region "$REGION" \
    --create-bucket-configuration LocationConstraint="$REGION"
fi
aws s3api put-bucket-versioning --bucket "$BUCKET" --versioning-configuration Status=Enabled
aws s3api put-bucket-encryption --bucket "$BUCKET" \
  --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
aws s3api put-public-access-block --bucket "$BUCKET" \
  --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true

DIR="$(cd "$(dirname "$0")/../infra/terraform" && pwd)"
cat > "$DIR/backend.hcl" <<EOF
bucket       = "${BUCKET}"
key          = "propertyhub/terraform.tfstate"
region       = "${REGION}"
encrypt      = true
use_lockfile = true
EOF
echo "State bucket ready: ${BUCKET}"
echo "Wrote ${DIR}/backend.hcl"
echo "Also set GitHub variable TF_STATE_BUCKET=${BUCKET}"

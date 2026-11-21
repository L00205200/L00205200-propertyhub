# GitHub Actions authenticates to AWS with short-lived OIDC tokens: no AWS access keys are stored in GitHub.
data "aws_caller_identity" "current" {}

resource "aws_iam_openid_connect_provider" "github" {
  count           = var.create_github_oidc_provider ? 1 : 0
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1", "1c58a3a8518e8759bf075b76b750d4f2df264fcd"]
}

locals {
  oidc_provider_arn = var.create_github_oidc_provider ? aws_iam_openid_connect_provider.github[0].arn : "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/token.actions.githubusercontent.com"
}

# ---------- Deploy role: may ONLY run SSM commands on the k3s node ----------
data "aws_iam_policy_document" "gha_deploy_trust" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    principals {
      type        = "Federated"
      identifiers = [local.oidc_provider_arn]
    }
    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }
    # Only jobs that run in the protected "production" environment of this repo
    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.github_repository}:environment:production"]
    }
  }
}

resource "aws_iam_role" "gha_deploy" {
  name                 = "${var.project}-github-deploy"
  assume_role_policy   = data.aws_iam_policy_document.gha_deploy_trust.json
  max_session_duration = 3600
}

data "aws_iam_policy_document" "gha_deploy" {
  statement {
    sid     = "RunDeployScriptOnNode"
    actions = ["ssm:SendCommand"]

    resources = [
      aws_instance.node.arn,
      "arn:aws:ssm:${var.aws_region}::document/AWS-RunShellScript",
    ]
  }
  statement {
    sid       = "ReadCommandResults"
    actions   = ["ssm:GetCommandInvocation", "ssm:ListCommandInvocations", "ec2:DescribeInstances"]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "gha_deploy" {
  name   = "${var.project}-github-deploy"
  role   = aws_iam_role.gha_deploy.id
  policy = data.aws_iam_policy_document.gha_deploy.json
}

# ---------- Terraform role: used by the infra workflow (environment "infrastructure", needs approval) ----------
data "aws_iam_policy_document" "gha_tf_trust" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    principals {
      type        = "Federated"
      identifiers = [local.oidc_provider_arn]
    }
    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }
    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.github_repository}:environment:infrastructure"]
    }
  }
}

resource "aws_iam_role" "gha_terraform" {
  name               = "${var.project}-github-terraform"
  assume_role_policy = data.aws_iam_policy_document.gha_tf_trust.json
}

resource "aws_iam_role_policy_attachment" "gha_terraform_poweruser" {
  role       = aws_iam_role.gha_terraform.name
  policy_arn = "arn:aws:iam::aws:policy/PowerUserAccess"
}

# PowerUser excludes IAM; allow IAM only on this project's own roles/policies/profiles and the OIDC provider.
data "aws_iam_policy_document" "gha_terraform_iam" {
  statement {
    actions = ["iam:*"]

    resources = [
      "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${var.project}-*",
      "arn:aws:iam::${data.aws_caller_identity.current.account_id}:instance-profile/${var.project}-*",
      "arn:aws:iam::${data.aws_caller_identity.current.account_id}:policy/${var.project}-*",
      "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/token.actions.githubusercontent.com",
    ]
  }
}

resource "aws_iam_role_policy" "gha_terraform_iam" {
  name   = "${var.project}-github-terraform-iam"
  role   = aws_iam_role.gha_terraform.id
  policy = data.aws_iam_policy_document.gha_terraform_iam.json
}

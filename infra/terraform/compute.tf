# Latest Ubuntu 24.04 LTS AMI published by Canonical
data "aws_ssm_parameter" "ubuntu" {
  name = "/aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id"
}

# ---------- Secrets: Grafana admin password lives only in SSM Parameter Store ----------
resource "random_password" "grafana_admin" {
  length  = 24
  special = false
}

resource "aws_ssm_parameter" "grafana_admin" {
  #checkov:skip=CKV_AWS_337:AWS-managed SSM key is sufficient for a lab; use a customer-managed KMS key in production
  name        = "/${var.project}/grafana/admin-password"
  description = "Grafana admin password (read by the node at boot)"
  type        = "SecureString"
  value       = random_password.grafana_admin.result
}

# ---------- Instance role: SSM agent + read its own parameters ----------
data "aws_iam_policy_document" "ec2_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "node" {
  name               = "${var.project}-node"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume.json
}

resource "aws_iam_role_policy_attachment" "node_ssm" {
  role       = aws_iam_role.node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

data "aws_iam_policy_document" "node_params" {
  statement {
    actions   = ["ssm:GetParameter", "ssm:GetParameters"]
    resources = ["arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter/${var.project}/*"]
  }
  statement {
    actions   = ["kms:Decrypt"]
    resources = ["*"]
    condition {
      test     = "StringEquals"
      variable = "kms:ViaService"
      values   = ["ssm.${var.aws_region}.amazonaws.com"]
    }
  }
}

resource "aws_iam_role_policy" "node_params" {
  name   = "${var.project}-node-params"
  role   = aws_iam_role.node.id
  policy = data.aws_iam_policy_document.node_params.json
}

resource "aws_iam_instance_profile" "node" {
  name = "${var.project}-node"
  role = aws_iam_role.node.name
}

# ---------- The k3s node ----------
resource "aws_instance" "node" {
  #checkov:skip=CKV_AWS_126:Detailed (1-minute) CloudWatch monitoring costs extra; Prometheus covers metrics
  ami                    = data.aws_ssm_parameter.ubuntu.value
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.node.id]
  iam_instance_profile   = aws_iam_instance_profile.node.name
  ebs_optimized          = true
  monitoring             = false

  metadata_options {
    http_tokens                 = "required" # IMDSv2 only
    http_put_response_hop_limit = 2
    http_endpoint               = "enabled"
  }

  root_block_device {
    volume_type = "gp3"
    volume_size = var.root_volume_gb
    encrypted   = true
  }

  user_data = templatefile("${path.module}/templates/bootstrap.sh.tftpl", {
    region        = var.aws_region
    grafana_param = aws_ssm_parameter.grafana_admin.name
    kps_version   = var.kube_prometheus_stack_version
    k3s_channel   = var.k3s_channel
    k3s_version   = var.k3s_version
  })
  user_data_replace_on_change = true

  tags = { Name = "${var.project}-k3s" }

  lifecycle {
    ignore_changes = [ami] # don't replace the node every time Canonical publishes a new AMI
  }
}

resource "aws_eip" "node" {
  domain   = "vpc"
  instance = aws_instance.node.id
  tags     = { Name = "${var.project}-eip" }
}

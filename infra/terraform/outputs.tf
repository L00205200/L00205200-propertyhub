output "aws_region" {
  description = "Set as GitHub variable AWS_REGION"
  value       = var.aws_region
}

output "instance_id" {
  description = "Set as GitHub variable EC2_INSTANCE_ID"
  value       = aws_instance.node.id
}

output "public_ip" {
  value = aws_eip.node.public_ip
}

output "app_url" {
  description = "Set as GitHub variable APP_URL"
  value       = "http://${aws_eip.node.public_ip}"
}

output "grafana_url" {
  value = "http://${aws_eip.node.public_ip}/grafana"
}

output "deploy_role_arn" {
  description = "Set as GitHub variable AWS_DEPLOY_ROLE_ARN"
  value       = aws_iam_role.gha_deploy.arn
}

output "terraform_role_arn" {
  description = "Set as GitHub variable AWS_TERRAFORM_ROLE_ARN"
  value       = aws_iam_role.gha_terraform.arn
}

output "grafana_password_command" {
  description = "Run this to read the Grafana admin password"
  value       = "aws ssm get-parameter --name ${aws_ssm_parameter.grafana_admin.name} --with-decryption --query Parameter.Value --output text --region ${var.aws_region}"
}

output "session_manager_command" {
  description = "Shell on the node without SSH"
  value       = "aws ssm start-session --target ${aws_instance.node.id} --region ${var.aws_region}"
}

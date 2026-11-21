variable "aws_region" {
  description = "AWS region (eu-west-1 = Ireland)"
  type        = string
  default     = "eu-west-1"
}

variable "project" {
  description = "Name prefix for all resources"
  type        = string
  default     = "propertyhub"
}

variable "owner" {
  description = "Owner tag (e.g. your student number)"
  type        = string
  default     = "student"
}

variable "github_repository" {
  description = "GitHub repository allowed to deploy, as owner/name (case-sensitive, e.g. L00205200/L00205200-propertyhub)"
  type        = string
}

variable "create_github_oidc_provider" {
  description = "Set false if the account already has the token.actions.githubusercontent.com OIDC provider"
  type        = bool
  default     = true
}

variable "instance_type" {
  description = "EC2 size. t3.large (8 GB) comfortably runs k3s + app + Prometheus/Grafana/Loki"
  type        = string
  default     = "t3.large"
}

variable "root_volume_gb" {
  type    = number
  default = 30
}

variable "allowed_http_cidrs" {
  description = "Who may reach the app/Grafana on port 80. Restrict to your IP (x.x.x.x/32) for extra safety."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "vpc_cidr" {
  type    = string
  default = "10.20.0.0/16"
}

variable "kube_prometheus_stack_version" {
  description = "Helm chart version for kube-prometheus-stack. Empty = latest. Pin after first install for reproducibility."
  type        = string
  default     = ""
}

variable "k3s_channel" {
  description = "k3s release channel (stable) or an exact version via k3s_version"
  type        = string
  default     = "stable"
}

variable "k3s_version" {
  description = "Exact k3s version, e.g. v1.31.4+k3s1. Empty = latest on k3s_channel."
  type        = string
  default     = ""
}

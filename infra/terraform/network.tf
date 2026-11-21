data "aws_availability_zones" "available" {
  #checkov:skip=CKV_AWS_394:Only the first AZ is used; result-set growth has no effect
  state = "available"
}

resource "aws_vpc" "main" {
  #checkov:skip=CKV2_AWS_11:VPC flow logs omitted to keep the student lab cost-free; enable for production
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags                 = { Name = "${var.project}-vpc" }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "${var.project}-igw" }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, 1)
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = false
  tags                    = { Name = "${var.project}-public" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = { Name = "${var.project}-public-rt" }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# Lock down the default security group (CIS benchmark)
resource "aws_default_security_group" "default" {
  vpc_id = aws_vpc.main.id
}

# HTTP only. No SSH: administration is through AWS Systems Manager Session Manager.
resource "aws_security_group" "node" {
  #checkov:skip=CKV_AWS_260:Public web app - port 80 must be reachable; narrow with var.allowed_http_cidrs
  #checkov:skip=CKV_AWS_382:Node needs outbound internet for k3s, Helm charts and container images
  name        = "${var.project}-node"
  description = "PropertyHub k3s node - HTTP in, all egress"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "HTTP to Traefik ingress (app and Grafana)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = var.allowed_http_cidrs
  }

  egress {
    description = "Outbound for packages, images and AWS APIs"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project}-node-sg" }
}

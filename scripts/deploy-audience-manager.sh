#!/bin/bash

# Audience Manager Deployment Script
# Deploys both API and UI components to Kubernetes

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="audience-manager-demo"
API_IMAGE="audience-manager-api:latest"
UI_IMAGE="audience-manager-ui:latest"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo -e "${BLUE}🚀 Audience Manager Deployment Script${NC}"
echo -e "${BLUE}======================================${NC}"

# Function to print status
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

# Check if namespace exists
if ! kubectl get namespace $NAMESPACE &> /dev/null; then
    print_warning "Namespace $NAMESPACE does not exist. Creating it..."
    kubectl create namespace $NAMESPACE
    print_status "Created namespace $NAMESPACE"
else
    print_status "Namespace $NAMESPACE already exists"
fi

# Build API Docker image
print_info "Building Audience Manager API Docker image..."
cd "$PROJECT_ROOT/audience-manager-api"

# Build the JAR if it doesn't exist
if [ ! -f target/audience-manager-api-*.jar ]; then
    print_info "Building API JAR file..."
    mvn clean package -DskipTests
    print_status "API JAR built successfully"
fi

# Build Docker image
docker build -t $API_IMAGE .
print_status "API Docker image built: $API_IMAGE"

# Build UI Docker image
print_info "Building Audience Manager UI Docker image..."
cd "$PROJECT_ROOT/audience-manager-ui"

# Build the React app if build directory doesn't exist
if [ ! -d "build" ]; then
    print_info "Building React application..."
    npm run build
    print_status "UI build completed successfully"
fi

# Build Docker image
docker build -t $UI_IMAGE .
print_status "UI Docker image built: $UI_IMAGE"

# Deploy to Kubernetes
print_info "Deploying to Kubernetes..."
cd "$PROJECT_ROOT"

# Deploy API
print_info "Deploying Audience Manager API..."
kubectl apply -f kube-config/deployment-specs/audience-manager-api-deployment.yaml
print_status "API deployment applied"

# Deploy UI
print_info "Deploying Audience Manager UI..."
kubectl apply -f kube-config/deployment-specs/audience-manager-ui-deployment.yaml
print_status "UI deployment applied"

# Wait for deployments to be ready
print_info "Waiting for deployments to be ready..."

echo "Waiting for API deployment..."
kubectl wait --for=condition=available --timeout=300s deployment/audience-manager-api -n $NAMESPACE

echo "Waiting for UI deployment..."
kubectl wait --for=condition=available --timeout=300s deployment/audience-manager-ui -n $NAMESPACE

print_status "All deployments are ready!"

# Get service information
print_info "Getting service information..."
echo ""
echo -e "${GREEN}=== Service Access Information ===${NC}"

API_NODEPORT=$(kubectl get service audience-manager-api-service -n $NAMESPACE -o jsonpath='{.spec.ports[0].nodePort}')
UI_NODEPORT=$(kubectl get service audience-manager-ui-service -n $NAMESPACE -o jsonpath='{.spec.ports[0].nodePort}')

echo -e "${BLUE}Audience Manager API:${NC}"
echo "  🌐 URL: http://localhost:$API_NODEPORT/api/v1"
echo "  📊 Health: http://localhost:$API_NODEPORT/actuator/health"
echo "  📈 Metrics: http://localhost:$API_NODEPORT/actuator/prometheus"
echo ""

echo -e "${BLUE}Audience Manager UI:${NC}"
echo "  🌐 URL: http://localhost:$UI_NODEPORT"
echo "  💚 Health: http://localhost:$UI_NODEPORT/health"
echo ""

echo -e "${BLUE}Other Infrastructure:${NC}"
echo "  🗄️  PostgreSQL: localhost:30432"
echo "  📡 Kafka: localhost:30092"
echo "  🔥 Flink UI: http://localhost:30081"
echo "  📊 Kafka UI: http://localhost:30090"
echo "  📈 Prometheus: http://localhost:30900"
echo "  📊 Grafana: http://localhost:30300"
echo ""

# Show pod status
echo -e "${GREEN}=== Pod Status ===${NC}"
kubectl get pods -n $NAMESPACE -l "app in (audience-manager-api,audience-manager-ui)" -o wide

echo ""
print_status "Deployment completed successfully! 🎉"
print_info "You can now access the Audience Manager UI at http://localhost:$UI_NODEPORT"
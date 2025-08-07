# Setup and Development Guide - Audience Manager Platform

## 1. Overview

This guide provides comprehensive instructions for setting up the Audience Manager Platform locally for development, testing, and demonstration purposes. The setup uses Kubernetes via Rancher Desktop to simulate a distributed environment on a single machine.

## 2. Prerequisites

### 2.1 System Requirements

**Minimum Requirements:**
- **OS**: macOS 10.15+, Windows 10+, or Linux (Ubuntu 20.04+)
- **RAM**: 8GB available (16GB total recommended)
- **CPU**: 4+ cores
- **Disk**: 20GB free space
- **Network**: Internet connection for downloads

**Recommended Specifications:**
- **RAM**: 12GB available (32GB total)
- **CPU**: 8+ cores
- **Disk**: 50GB SSD free space

### 2.2 Required Software

#### Core Development Tools
```bash
# macOS (using Homebrew)
brew install git
brew install openjdk@11
brew install node@18
brew install maven
brew install kubectl

# Ubuntu/Debian
sudo apt update
sudo apt install git openjdk-11-jdk nodejs npm maven-debian-helper kubectl

# Windows (using Chocolatey)
choco install git
choco install openjdk11
choco install nodejs
choco install maven
choco install kubernetes-cli
```

#### Container and Kubernetes Platform
1. **Rancher Desktop** (Recommended)
   - Download from: https://rancherdesktop.io/
   - Select Kubernetes version 1.27+
   - Allocate 8GB RAM minimum
   - Use dockerd as container runtime

2. **Alternative: Docker Desktop + Kind**
   ```bash
   # Install Docker Desktop
   # Then install Kind
   brew install kind  # macOS
   # OR
   curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64
   chmod +x ./kind
   sudo mv ./kind /usr/local/bin/kind
   ```

#### Database Tools (Optional)
```bash
# PostgreSQL client
brew install postgresql@15  # macOS
sudo apt install postgresql-client-15  # Ubuntu

# Aerospike client
# Download from: https://aerospike.com/download/tools/
```

## 3. Project Structure

```
audience-manager/
├── audience-manager-api/          # Spring Boot API service
│   ├── src/main/java/             # Java source code
│   ├── src/main/resources/        # Configuration files
│   ├── Dockerfile                 # Container image definition
│   └── pom.xml                    # Maven dependencies
├── audience-manager-ui/           # React TypeScript UI
│   ├── src/                       # React source code
│   ├── public/                    # Static assets
│   ├── Dockerfile                 # Container image definition
│   ├── nginx.conf                 # Nginx configuration
│   └── package.json               # Node.js dependencies
├── kube-config/                   # Kubernetes configurations
│   └── deployment-specs/          # K8s manifests
├── docs/                          # Documentation
└── scripts/                       # Utility scripts
```

## 4. Initial Setup

### 4.1 Clone the Repository

```bash
# Clone the project
git clone <repository-url>
cd audience-manager

# Verify project structure
ls -la
```

### 4.2 Configure Rancher Desktop

1. **Install and Launch Rancher Desktop**
   ```bash
   # Download and install from https://rancherdesktop.io/
   # Launch the application
   ```

2. **Configure Resources**
   - Open Rancher Desktop
   - Go to Preferences → Resources
   - Set Memory: 8GB (minimum)
   - Set CPUs: 4 (minimum)
   - Apply and restart

3. **Verify Kubernetes Setup**
   ```bash
   # Check cluster status
   kubectl cluster-info
   
   # Check node resources
   kubectl top nodes
   
   # Create namespace
   kubectl create namespace audience-manager-demo
   ```

### 4.3 Set Environment Variables

```bash
# Add to ~/.bashrc, ~/.zshrc, or equivalent
export JAVA_HOME=/opt/homebrew/opt/openjdk@11  # macOS with Homebrew
export PATH=$JAVA_HOME/bin:$PATH
export KUBECONFIG=~/.kube/config

# Verify Java version
java -version  # Should show Java 11

# Verify Node.js version
node --version  # Should show v18+
npm --version
```

## 5. Build Process

### 5.1 Build API Service

```bash
# Navigate to API directory
cd audience-manager-api

# Verify Maven setup
mvn --version

# Clean and compile
mvn clean compile

# Run tests (optional)
mvn test

# Package application
mvn clean package -DskipTests

# Verify JAR file creation
ls -la target/*.jar
```

**Expected Output:**
```
target/audience-manager-api-2.7.18.jar
```

### 5.2 Build UI Application

```bash
# Navigate to UI directory
cd ../audience-manager-ui

# Install dependencies
npm install

# Verify dependencies
npm list --depth=0

# Build for production
npm run build

# Verify build output
ls -la build/
```

**Expected Output:**
```
build/
├── static/
│   ├── css/
│   └── js/
├── index.html
└── ...
```

### 5.3 Build Docker Images

```bash
# Return to project root
cd ..

# Build API image
docker build -t audience-manager-api:latest audience-manager-api/

# Build UI image
docker build -t audience-manager-ui:latest audience-manager-ui/

# Verify images
docker images | grep audience-manager
```

**Expected Output:**
```
audience-manager-api    latest    <image-id>    <time>    <size>
audience-manager-ui     latest    <image-id>    <time>    <size>
```

## 6. Infrastructure Deployment

### 6.1 Deploy Core Infrastructure

```bash
# Apply namespace configuration
kubectl apply -f kube-config/deployment-specs/namespace.yaml

# Deploy PostgreSQL database
kubectl apply -f kube-config/deployment-specs/postgres-deployment.yaml

# Deploy Aerospike database
kubectl apply -f kube-config/deployment-specs/aerospike-deployment.yaml

# Verify database deployments
kubectl get pods -n audience-manager-demo
```

### 6.2 Deploy Messaging Infrastructure

```bash
# Deploy Zookeeper and Kafka
kubectl apply -f kube-config/deployment-specs/kafka-deployment.yaml

# Deploy Kafka UI (optional)
kubectl apply -f kube-config/deployment-specs/kafka-ui-deployment.yaml

# Wait for Kafka to be ready
kubectl wait --for=condition=ready pod -l app=kafka -n audience-manager-demo --timeout=300s
```

### 6.3 Deploy Stream Processing

```bash
# Deploy Flink cluster
kubectl apply -f kube-config/deployment-specs/flink-deployment.yaml

# Verify Flink deployment
kubectl get pods -l app=flink-jobmanager -n audience-manager-demo
kubectl get pods -l app=flink-taskmanager -n audience-manager-demo
```

### 6.4 Deploy Monitoring Stack

```bash
# Deploy Prometheus
kubectl apply -f kube-config/deployment-specs/monitoring-deployment.yaml

# Verify monitoring stack
kubectl get pods -l app=prometheus -n audience-manager-demo
kubectl get pods -l app=grafana -n audience-manager-demo
```

## 7. Application Deployment

### 7.1 Deploy API Service

```bash
# Deploy API service
kubectl apply -f kube-config/deployment-specs/audience-manager-api.yaml

# Wait for deployment
kubectl wait --for=condition=available deployment/audience-manager-api -n audience-manager-demo --timeout=300s

# Check deployment status
kubectl get deployment audience-manager-api -n audience-manager-demo
kubectl get pods -l app=audience-manager-api -n audience-manager-demo
```

### 7.2 Deploy UI Service

```bash
# Deploy UI service
kubectl apply -f kube-config/deployment-specs/audience-manager-ui.yaml

# Wait for deployment
kubectl wait --for=condition=available deployment/audience-manager-ui -n audience-manager-demo --timeout=300s

# Check deployment status
kubectl get deployment audience-manager-ui -n audience-manager-demo
kubectl get pods -l app=audience-manager-ui -n audience-manager-demo
```

### 7.3 Verify Complete Deployment

```bash
# Check all pods
kubectl get pods -n audience-manager-demo -o wide

# Check services
kubectl get services -n audience-manager-demo

# Check ingress/nodeports
kubectl get svc -n audience-manager-demo --no-headers | grep NodePort
```

**Expected Output:**
```
NAME                         TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)
audience-manager-api-service NodePort   10.43.xxx.xxx   <none>        8080:30280/TCP
audience-manager-ui-service  NodePort   10.43.xxx.xxx   <none>        80:30280/TCP
postgres-service             NodePort   10.43.xxx.xxx   <none>        5432:30432/TCP
...
```

## 8. Verification and Testing

### 8.1 Health Checks

```bash
# Check API health
curl http://localhost:30280/api/v1/segments/health

# Check UI accessibility
curl -I http://localhost:30280

# Check database connectivity
kubectl exec -it -n audience-manager-demo deployment/postgres -- psql -U postgres -d audience_manager -c "SELECT version();"
```

### 8.2 Functional Testing

**Create a Test Segment:**
```bash
curl -X POST http://localhost:30280/api/v1/segments \
  -H "Content-Type: application/json" \
  -d '{
    "name": "High Engagement Users",
    "description": "Users with more than 50 clicks in last 10 minutes",
    "type": "INDEPENDENT",
    "windowMinutes": 10,
    "rules": [
      {
        "eventType": "clicks",
        "attribute": "count",
        "operator": "GT",
        "value": "50"
      }
    ]
  }'
```

**Retrieve Segments:**
```bash
curl http://localhost:30280/api/v1/segments
```

**Test UI Features:**
1. Open http://localhost:30280 in browser
2. Navigate to Segments page
3. Create a new segment using the form
4. View segment details
5. Edit the segment

### 8.3 Database Verification

**PostgreSQL:**
```bash
# Connect to PostgreSQL
kubectl exec -it -n audience-manager-demo deployment/postgres -- psql -U postgres -d audience_manager

# Check tables
\dt

# Query segments
SELECT id, name, type, active FROM segments;

# Exit
\q
```

**Aerospike:**
```bash
# Install AQL client (if not done)
# Download from: https://aerospike.com/download/tools/

# Connect to Aerospike
aql -h localhost -p 30300

# Show namespaces
show namespaces;

# Check sets (if any data exists)
show sets;

# Exit
exit
```

## 9. Development Workflow

### 9.1 Local Development Setup

**API Development:**
```bash
# Navigate to API directory
cd audience-manager-api

# Run locally with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or with external database
mvn spring-boot:run -Dspring-boot.run.profiles=dev \
  -Dspring.datasource.url=jdbc:postgresql://localhost:30432/audience_manager
```

**UI Development:**
```bash
# Navigate to UI directory
cd audience-manager-ui

# Start development server
npm start

# The UI will be available at http://localhost:3000
# API calls will proxy to http://localhost:30280
```

### 9.2 Hot Reloading Development

**API Hot Reload:**
```bash
# Install Spring Boot DevTools (already included)
# Enable auto-restart in IDE or use:
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

**UI Hot Reload:**
```bash
# Start with hot reloading (default)
npm start

# Or with custom proxy
REACT_APP_API_URL=http://localhost:8080/api/v1 npm start
```

### 9.3 Testing in Kubernetes

**Deploy Changes:**
```bash
# Rebuild and deploy API
cd audience-manager-api
mvn clean package -DskipTests
cd ..
docker build -t audience-manager-api:latest audience-manager-api/
kubectl rollout restart deployment/audience-manager-api -n audience-manager-demo

# Rebuild and deploy UI
cd audience-manager-ui
npm run build
cd ..
docker build -t audience-manager-ui:latest audience-manager-ui/
kubectl rollout restart deployment/audience-manager-ui -n audience-manager-demo
```

**Monitor Deployments:**
```bash
# Watch pod status
kubectl get pods -n audience-manager-demo -w

# Follow logs
kubectl logs -f deployment/audience-manager-api -n audience-manager-demo
kubectl logs -f deployment/audience-manager-ui -n audience-manager-demo
```

## 10. Debugging and Troubleshooting

### 10.1 Common Issues

**Issue: Pod in CrashLoopBackOff**
```bash
# Check pod logs
kubectl logs <pod-name> -n audience-manager-demo

# Describe pod for events
kubectl describe pod <pod-name> -n audience-manager-demo

# Check resource limits
kubectl top pods -n audience-manager-demo
```

**Issue: API Connection Refused**
```bash
# Check service endpoints
kubectl get endpoints -n audience-manager-demo

# Port forward for direct access
kubectl port-forward service/audience-manager-api-service 8080:8080 -n audience-manager-demo

# Test direct connection
curl http://localhost:8080/actuator/health
```

**Issue: Database Connection Failed**
```bash
# Check PostgreSQL pod
kubectl logs deployment/postgres -n audience-manager-demo

# Verify database creation
kubectl exec -it deployment/postgres -n audience-manager-demo -- psql -U postgres -l

# Test connection from API pod
kubectl exec -it deployment/audience-manager-api -n audience-manager-demo -- nc -zv postgres-service 5432
```

### 10.2 Log Analysis

**Centralized Logging:**
```bash
# View all logs for a service
kubectl logs deployment/audience-manager-api -n audience-manager-demo --tail=100

# Follow logs in real-time
kubectl logs -f deployment/audience-manager-api -n audience-manager-demo

# Export logs to file
kubectl logs deployment/audience-manager-api -n audience-manager-demo > api.log
```

**Application Metrics:**
```bash
# Access Prometheus metrics
curl http://localhost:30280/actuator/prometheus

# Check application health
curl http://localhost:30280/actuator/health

# View application info
curl http://localhost:30280/actuator/info
```

### 10.3 Performance Monitoring

**Resource Usage:**
```bash
# Check pod resource usage
kubectl top pods -n audience-manager-demo

# Check node resource usage
kubectl top nodes

# Describe resource limits
kubectl describe pod <pod-name> -n audience-manager-demo | grep -A 5 "Limits\|Requests"
```

**Database Performance:**
```bash
# PostgreSQL performance
kubectl exec -it deployment/postgres -n audience-manager-demo -- psql -U postgres -d audience_manager -c "
SELECT 
  schemaname,
  tablename,
  n_tup_ins as inserts,
  n_tup_upd as updates,
  n_tup_del as deletes
FROM pg_stat_user_tables;"

# Aerospike statistics
aql -h localhost -p 30300 -c "show statistics;"
```

## 11. Data Management

### 11.1 Database Migration

**PostgreSQL Schema Updates:**
```bash
# Connect to database
kubectl exec -it deployment/postgres -n audience-manager-demo -- psql -U postgres -d audience_manager

# View current schema
\d segments;
\d segment_rules;

# Apply schema changes (if needed)
# Note: Spring Boot handles schema evolution automatically with ddl-auto: update
```

**Data Backup:**
```bash
# Backup PostgreSQL
kubectl exec deployment/postgres -n audience-manager-demo -- pg_dump -U postgres audience_manager > backup.sql

# Restore PostgreSQL
kubectl exec -i deployment/postgres -n audience-manager-demo -- psql -U postgres audience_manager < backup.sql
```

### 11.2 Sample Data Loading

**Load Test Data:**
```bash
# Create sample segments via API
cat > sample-segments.json << 'EOF'
[
  {
    "name": "Mobile Users",
    "description": "Users on mobile devices",
    "type": "INDEPENDENT",
    "windowMinutes": 60,
    "rules": [
      {
        "eventType": "clicks",
        "attribute": "count",
        "operator": "GT",
        "value": "5"
      }
    ]
  },
  {
    "name": "High Value Customers",
    "description": "Customers with high order values",
    "type": "INDEPENDENT",
    "windowMinutes": 1440,
    "rules": [
      {
        "eventType": "orders",
        "attribute": "sum",
        "operator": "GTE",
        "value": "100"
      }
    ]
  }
]
EOF

# Load using curl
for segment in $(cat sample-segments.json | jq -c '.[]'); do
  curl -X POST http://localhost:30280/api/v1/segments \
    -H "Content-Type: application/json" \
    -d "$segment"
done
```

## 12. Security Configuration

### 12.1 Development Security

**Default Credentials (Development Only):**
- PostgreSQL: `postgres/demo123`
- Grafana: `admin/admin`
- All services run with default security for ease of development

**Network Security:**
```bash
# Check network policies (if any)
kubectl get networkpolicies -n audience-manager-demo

# View service exposure
kubectl get svc -n audience-manager-demo
```

### 12.2 Production Security Considerations

**Secrets Management:**
```bash
# Create secrets for production
kubectl create secret generic db-credentials \
  --from-literal=username=postgres \
  --from-literal=password=<secure-password> \
  -n audience-manager-demo

# Create TLS certificates
kubectl create secret tls audience-manager-tls \
  --cert=path/to/cert.crt \
  --key=path/to/cert.key \
  -n audience-manager-demo
```

## 13. Maintenance and Updates

### 13.1 Regular Maintenance

**Weekly Tasks:**
```bash
# Update dependencies
cd audience-manager-api
mvn versions:display-dependency-updates

cd ../audience-manager-ui
npm audit
npm update

# Clean up unused resources
kubectl delete pods --field-selector=status.phase=Succeeded -n audience-manager-demo
```

**Monthly Tasks:**
```bash
# Update base images
docker pull postgres:15-alpine
docker pull confluentinc/cp-kafka:7.4.0
docker pull flink:1.18.0-scala_2.12-java11

# Review resource usage
kubectl top pods -n audience-manager-demo
kubectl describe nodes
```

### 13.2 Scaling for Load Testing

**Scale Up for Testing:**
```bash
# Scale API service
kubectl scale deployment audience-manager-api --replicas=3 -n audience-manager-demo

# Scale Flink TaskManagers
kubectl scale deployment flink-taskmanager --replicas=2 -n audience-manager-demo

# Monitor scaling
kubectl get hpa -n audience-manager-demo -w
```

**Scale Down After Testing:**
```bash
# Scale back to development configuration
kubectl scale deployment audience-manager-api --replicas=1 -n audience-manager-demo
kubectl scale deployment flink-taskmanager --replicas=1 -n audience-manager-demo
```

## 14. IDE Configuration

### 14.1 IntelliJ IDEA Setup

**Project Import:**
1. File → Open → Select `audience-manager` directory
2. Import as Maven project
3. Set Project SDK to Java 11
4. Enable annotation processing
5. Install Spring Boot plugin

**Run Configurations:**
```xml
<!-- API Service -->
<configuration name="AudienceManagerAPI" type="SpringBootApplicationConfigurationType">
  <option name="SPRING_BOOT_MAIN_CLASS" value="com.audiencemanager.api.SegmentManagementApplication"/>
  <option name="ACTIVE_PROFILES" value="dev"/>
  <option name="VM_PARAMETERS" value="-Xmx1g"/>
</configuration>
```

### 14.2 VS Code Setup

**Extensions:**
- Java Extension Pack
- Spring Boot Extension Pack
- Docker
- Kubernetes
- ES7+ React/Redux/React-Native snippets
- TypeScript Importer

**Settings (`.vscode/settings.json`):**
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.home": "/opt/homebrew/opt/openjdk@11",
  "typescript.preferences.importModuleSpecifier": "relative",
  "editor.formatOnSave": true
}
```

## 15. Cleanup and Teardown

### 15.1 Partial Cleanup

**Remove Application Only:**
```bash
# Delete application deployments
kubectl delete deployment audience-manager-api audience-manager-ui -n audience-manager-demo

# Keep infrastructure running for development
```

**Remove Infrastructure:**
```bash
# Delete all infrastructure
kubectl delete -f kube-config/deployment-specs/ --ignore-not-found=true
```

### 15.2 Complete Cleanup

**Remove Everything:**
```bash
# Delete entire namespace
kubectl delete namespace audience-manager-demo

# Remove Docker images
docker rmi audience-manager-api:latest
docker rmi audience-manager-ui:latest

# Clean up Docker system
docker system prune -f
```

**Reset Rancher Desktop:**
```bash
# Reset Kubernetes cluster (if needed)
# Go to Rancher Desktop → Troubleshooting → Reset Kubernetes
```

## 16. Quick Reference

### 16.1 Essential Commands

```bash
# Build and deploy everything
./scripts/build-and-deploy.sh

# Check all services
kubectl get pods,svc -n audience-manager-demo

# Access UIs
open http://localhost:30280          # Audience Manager UI
open http://localhost:30001          # Grafana
open http://localhost:30080          # Kafka UI
open http://localhost:30881          # Flink UI

# View logs
kubectl logs -f deployment/audience-manager-api -n audience-manager-demo

# Scale services
kubectl scale deployment audience-manager-api --replicas=2 -n audience-manager-demo

# Port forward
kubectl port-forward service/audience-manager-api-service 8080:8080 -n audience-manager-demo
```

### 16.2 Useful Aliases

```bash
# Add to ~/.bashrc or ~/.zshrc
alias k='kubectl'
alias kgp='kubectl get pods -n audience-manager-demo'
alias kgs='kubectl get svc -n audience-manager-demo'
alias klogs='kubectl logs -f -n audience-manager-demo'
alias kexec='kubectl exec -it -n audience-manager-demo'

# Usage examples
kgp                              # List pods
klogs deployment/audience-manager-api  # Follow API logs
kexec deployment/postgres -- psql -U postgres
```

### 16.3 URLs and Endpoints

| Service | URL | Purpose |
|---------|-----|---------|
| Audience Manager UI | http://localhost:30280 | Main application interface |
| API Health Check | http://localhost:30280/api/v1/segments/health | API status |
| API Documentation | http://localhost:30280/swagger-ui.html | OpenAPI docs |
| Grafana | http://localhost:30001 | Monitoring dashboards |
| Prometheus | http://localhost:30090 | Metrics collection |
| Kafka UI | http://localhost:30080 | Kafka topic management |
| Flink UI | http://localhost:30881 | Stream processing jobs |

---

This setup guide provides everything needed to get the Audience Manager Platform running locally for development and testing. Follow the sections sequentially for a complete setup, or use individual sections for specific tasks and troubleshooting.
#!/bin/bash

# Audience Manager Environment Setup Script
# This script sets up the complete development environment for the Audience Manager platform

set -e

echo "🚀 Setting up Audience Manager development environment..."

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command_exists docker; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command_exists docker-compose; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

if ! command_exists mvn; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

if ! command_exists java; then
    echo "❌ Java is not installed. Please install Java 17+ first."
    exit 1
fi

echo "✅ All prerequisites are installed"

# Navigate to the infrastructure directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$(dirname "$SCRIPT_DIR")"
PROJECT_ROOT="$(dirname "$INFRA_DIR")"

cd "$INFRA_DIR/docker"

# Start infrastructure services
echo "🐳 Starting infrastructure services..."
docker-compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."

# Wait for Kafka
echo "Waiting for Kafka..."
timeout 60 bash -c 'until docker exec audience-manager-kafka kafka-topics --list --bootstrap-server localhost:9092 > /dev/null 2>&1; do sleep 2; done'

# Wait for Aerospike
echo "Waiting for Aerospike..."
timeout 60 bash -c 'until docker exec audience-manager-aerospike asinfo -v "status" > /dev/null 2>&1; do sleep 2; done'

# Wait for PostgreSQL
echo "Waiting for PostgreSQL..."
timeout 60 bash -c 'until docker exec audience-manager-postgres pg_isready -U postgres > /dev/null 2>&1; do sleep 2; done'

echo "✅ Infrastructure services are ready!"

# Create Kafka topics
echo "📝 Creating Kafka topics..."
docker exec audience-manager-kafka kafka-topics \
    --create \
    --topic user-events \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1 \
    --if-not-exists

docker exec audience-manager-kafka kafka-topics \
    --create \
    --topic segment-updates \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1 \
    --if-not-exists

docker exec audience-manager-kafka kafka-topics \
    --create \
    --topic user-segments \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1 \
    --if-not-exists

docker exec audience-manager-kafka kafka-topics \
    --create \
    --topic notification-requests \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1 \
    --if-not-exists

echo "✅ Kafka topics created successfully!"

# Build the project
echo "🔨 Building the project..."
cd "$PROJECT_ROOT"
mvn clean install -DskipTests

echo "✅ Project built successfully!"

# Create SQLite database
echo "🗄️ Setting up SQLite database..."
mkdir -p "$PROJECT_ROOT/data"
SQLITE_DB="$PROJECT_ROOT/data/audience_manager.db"

# Initialize database schema (this would be done by the API service on startup)
echo "Database will be initialized by the API service on first startup."

echo ""
echo "🎉 Environment setup complete!"
echo ""
echo "📊 Access the services:"
echo "  - Kafka UI: http://localhost:8080"
echo "  - Flink Dashboard: http://localhost:8081"
echo "  - Prometheus: http://localhost:9090"
echo "  - Grafana: http://localhost:3001 (admin/admin123)"
echo ""
echo "🚀 Next steps:"
echo "  1. Start the Segment API service: cd audience-manager-api && mvn spring-boot:run"
echo "  2. Start the Ingestion service: cd audience-manager-ingestion && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082"
echo "  3. Start the Communication service: cd audience-manager-communication && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8083"
echo "  4. Deploy the Flink processor job with integrated rule engine: ./audience-manager-infrastructure/scripts/deploy-flink-job.sh"
echo ""
echo "📖 Check the documentation in the docs/ folder for more details."
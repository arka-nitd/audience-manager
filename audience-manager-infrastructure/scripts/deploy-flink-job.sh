#!/bin/bash

# Flink Job Deployment Script for Audience Manager
# This script builds and deploys the stream processing job to Flink

set -e

echo "🚀 Deploying Audience Manager Flink job..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"
PROCESSOR_DIR="$PROJECT_ROOT/audience-manager-processor"

# Check if Flink is running
if ! curl -s http://localhost:8081/overview > /dev/null; then
    echo "❌ Flink JobManager is not accessible at http://localhost:8081"
    echo "Please make sure the infrastructure is running: ./setup-environment.sh"
    exit 1
fi

echo "✅ Flink JobManager is accessible"

# Build the processor module
echo "🔨 Building the processor module..."
cd "$PROJECT_ROOT"
mvn clean package -pl audience-manager-processor -am -DskipTests

# Check if jar file exists
JAR_FILE="$PROCESSOR_DIR/target/audience-manager-processor-1.0-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "Build might have failed."
    exit 1
fi

echo "✅ JAR file built successfully"

# Cancel any existing jobs (optional)
echo "🔍 Checking for existing jobs..."
EXISTING_JOBS=$(curl -s http://localhost:8081/jobs | jq -r '.jobs[].id // empty')

if [ ! -z "$EXISTING_JOBS" ]; then
    echo "Found existing jobs. Cancelling them..."
    for job_id in $EXISTING_JOBS; do
        echo "Cancelling job: $job_id"
        curl -X PATCH http://localhost:8081/jobs/$job_id?mode=cancel || true
        sleep 2
    done
fi

# Submit the new job
echo "📤 Submitting the Flink job..."

# Copy JAR to Flink container
docker cp "$JAR_FILE" audience-manager-flink-jobmanager:/opt/flink/audience-manager-processor.jar

# Submit job via Flink REST API
JOB_RESPONSE=$(docker exec audience-manager-flink-jobmanager flink run \
    --class com.audiencemanager.processor.SegmentProcessorApplication \
    --parallelism 2 \
    /opt/flink/audience-manager-processor.jar)

echo "📋 Job submission response:"
echo "$JOB_RESPONSE"

# Extract job ID (this is a simplified approach)
sleep 5
JOBS=$(curl -s http://localhost:8081/jobs)
echo "📊 Current jobs:"
echo "$JOBS" | jq .

echo ""
echo "✅ Flink job deployment complete!"
echo ""
echo "🔗 Monitor the job at: http://localhost:8081"
echo "📈 Check job metrics and logs in the Flink dashboard"
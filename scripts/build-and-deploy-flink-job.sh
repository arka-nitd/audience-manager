#!/bin/bash

# Build and deploy Flink job for segment processing
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🚀 Building and deploying Flink Segment Processing Job..."
echo "Project root: $PROJECT_ROOT"

# Step 1: Build the Flink job JAR
echo ""
echo "📦 Step 1: Building Flink job JAR..."
cd "$PROJECT_ROOT"

echo "Building audience-manager-ingestion module..."
mvn clean package -pl audience-manager-ingestion -am -DskipTests

# Check if JAR was created
FLINK_JAR="$PROJECT_ROOT/audience-manager-ingestion/target/audience-manager-ingestion-1.0-SNAPSHOT.jar"
if [ ! -f "$FLINK_JAR" ]; then
    echo "❌ Error: Flink JAR not found at $FLINK_JAR"
    echo "Build may have failed. Check the output above."
    exit 1
fi

echo "✅ Flink JAR built successfully: $FLINK_JAR"
echo "JAR size: $(du -h "$FLINK_JAR" | cut -f1)"

# Step 2: Setup port forwarding for Flink JobManager
echo ""
echo "🌐 Step 2: Setting up port forwarding for Flink JobManager..."

# Check if port forwarding is already running
if ! lsof -i :8081 > /dev/null 2>&1; then
    echo "Starting port forwarding for Flink JobManager..."
    kubectl port-forward -n audience-manager-demo svc/flink-jobmanager 8081:8081 > /dev/null 2>&1 &
    PORT_FORWARD_PID=$!
    
    # Wait a moment for port forwarding to establish
    sleep 3
    
    # Verify port forwarding is working
    if ! lsof -i :8081 > /dev/null 2>&1; then
        echo "❌ Failed to establish port forwarding to Flink JobManager"
        exit 1
    fi
    
    echo "✅ Port forwarding established (PID: $PORT_FORWARD_PID)"
else
    echo "✅ Port forwarding already active on port 8081"
fi

# Step 3: Wait for Flink JobManager to be ready
echo ""
echo "⏳ Step 3: Waiting for Flink JobManager to be ready..."

max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -s http://localhost:8081/overview > /dev/null 2>&1; then
        echo "✅ Flink JobManager is ready"
        break
    fi
    
    attempt=$((attempt + 1))
    echo "Waiting for Flink JobManager... ($attempt/$max_attempts)"
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "❌ Timeout waiting for Flink JobManager to be ready"
    echo "Please check:"
    echo "  kubectl get pods -n audience-manager-demo | grep flink"
    echo "  kubectl logs -n audience-manager-demo deployment/flink-jobmanager"
    exit 1
fi

# Step 4: Submit the Flink job
echo ""
echo "📤 Step 4: Submitting Flink job..."

# Create job submission payload
JOB_NAME="segment-processing-job"
PARALLELISM=2
ENTRY_CLASS="com.audiencemanager.ingestion.SegmentProcessingJob"

# Submit job using Flink REST API
echo "Submitting job: $JOB_NAME"
echo "Entry class: $ENTRY_CLASS"
echo "Parallelism: $PARALLELISM"

# First, upload the JAR
echo "Uploading JAR file..."
JAR_UPLOAD_RESPONSE=$(curl -s -X POST -H "Content-Type: application/java-archive" \
    --data-binary "@$FLINK_JAR" \
    http://localhost:8081/jars/upload)

echo "JAR upload response: $JAR_UPLOAD_RESPONSE"

# Extract JAR ID from response
JAR_ID=$(echo "$JAR_UPLOAD_RESPONSE" | grep -o '"filename":"[^"]*"' | cut -d'"' -f4)

if [ -z "$JAR_ID" ]; then
    echo "❌ Failed to extract JAR ID from upload response"
    echo "Response: $JAR_UPLOAD_RESPONSE"
    exit 1
fi

echo "✅ JAR uploaded successfully with ID: $JAR_ID"

# Submit the job
echo "Submitting job..."
JOB_SUBMIT_RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d "{
        \"entryClass\": \"$ENTRY_CLASS\",
        \"parallelism\": $PARALLELISM,
        \"programArgs\": \"\"
    }" \
    "http://localhost:8081/jars/$JAR_ID/run")

echo "Job submission response: $JOB_SUBMIT_RESPONSE"

# Extract job ID
JOB_ID=$(echo "$JOB_SUBMIT_RESPONSE" | grep -o '"jobid":"[^"]*"' | cut -d'"' -f4)

if [ -z "$JOB_ID" ]; then
    echo "❌ Failed to submit job or extract job ID"
    echo "Response: $JOB_SUBMIT_RESPONSE"
    exit 1
fi

echo "✅ Job submitted successfully with ID: $JOB_ID"

# Step 5: Monitor job status
echo ""
echo "📊 Step 5: Monitoring job status..."

max_wait=60
wait_time=0
while [ $wait_time -lt $max_wait ]; do
    JOB_STATUS=$(curl -s "http://localhost:8081/jobs/$JOB_ID" | grep -o '"state":"[^"]*"' | cut -d'"' -f4)
    
    echo "Job status: $JOB_STATUS (${wait_time}s elapsed)"
    
    case "$JOB_STATUS" in
        "RUNNING")
            echo "✅ Job is running successfully!"
            break
            ;;
        "FAILED"|"CANCELED")
            echo "❌ Job failed or was canceled"
            echo "Check job details: http://localhost:8081/#/job/$JOB_ID/overview"
            exit 1
            ;;
        "FINISHED")
            echo "ℹ️  Job completed (this shouldn't happen for a streaming job)"
            break
            ;;
    esac
    
    sleep 5
    wait_time=$((wait_time + 5))
done

# Step 6: Display useful information
echo ""
echo "🎉 Flink Job Deployment Completed!"
echo ""
echo "Job Details:"
echo "  Job ID: $JOB_ID"
echo "  Job Name: $JOB_NAME"
echo "  Status: $JOB_STATUS"
echo "  Entry Class: $ENTRY_CLASS"
echo ""
echo "Useful Links:"
echo "  Flink Dashboard: http://localhost:8081"
echo "  Job Overview: http://localhost:8081/#/job/$JOB_ID/overview"
echo "  Job Logs: http://localhost:8081/#/job/$JOB_ID/exceptions"
echo ""
echo "Monitoring Commands:"
echo "  # Check running jobs"
echo "  curl http://localhost:8081/jobs"
echo ""
echo "  # Check job details"
echo "  curl http://localhost:8081/jobs/$JOB_ID"
echo ""
echo "  # Check TaskManager logs"
echo "  kubectl logs -n audience-manager-demo deployment/flink-taskmanager"
echo ""
echo "  # Check JobManager logs"
echo "  kubectl logs -n audience-manager-demo deployment/flink-jobmanager"
echo ""
echo "To stop the job:"
echo "  curl -X PATCH http://localhost:8081/jobs/$JOB_ID?mode=cancel"
echo ""

echo "✅ Deployment script completed successfully!"
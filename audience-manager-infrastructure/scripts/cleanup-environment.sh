#!/bin/bash

# Audience Manager Environment Cleanup Script
# This script cleans up the development environment

set -e

echo "🧹 Cleaning up Audience Manager development environment..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$(dirname "$SCRIPT_DIR")"

cd "$INFRA_DIR/docker"

# Stop and remove containers
echo "🛑 Stopping and removing containers..."
docker-compose down

# Remove volumes (optional - ask user)
read -p "Do you want to remove all data volumes? This will delete all data! (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️ Removing volumes..."
    docker-compose down -v
    echo "✅ Volumes removed"
else
    echo "📦 Keeping volumes (data preserved)"
fi

# Remove Docker images (optional - ask user)
read -p "Do you want to remove Docker images? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️ Removing Docker images..."
    docker-compose down --rmi all
    echo "✅ Images removed"
else
    echo "📦 Keeping Docker images"
fi

# Clean up Maven artifacts
read -p "Do you want to clean Maven build artifacts? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🧹 Cleaning Maven artifacts..."
    cd "$(dirname "$INFRA_DIR")"
    mvn clean
    echo "✅ Maven artifacts cleaned"
fi

echo ""
echo "✅ Cleanup complete!"
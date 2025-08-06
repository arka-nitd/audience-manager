# Production vs Demo Architecture Comparison

## 📊 Overview

This document provides a detailed comparison between the **Production Architecture** (defined in [HLD](../hld/system-architecture.md)) and the **Demo Architecture** optimized for single-machine Kubernetes deployment with 8GB memory.

## 🏗️ Architecture Comparison

### High-Level Differences

| Aspect | Production | Demo | Reasoning |
|--------|------------|------|-----------|
| **Target Environment** | Multi-node Kubernetes cluster | Single-node Rancher Desktop | Resource constraints |
| **Memory Allocation** | 50-100GB+ | 8GB total | Development/demo budget |
| **High Availability** | Multi-replica, cross-AZ | Single replica | Simplicity over availability |
| **Data Persistence** | Persistent volumes, backups | EmptyDir (ephemeral) | Demo data is disposable |
| **Security** | Full TLS, RBAC, network policies | Basic auth, no TLS | Demo simplicity |
| **Monitoring** | Full observability stack | Essential monitoring only | Resource optimization |

## 🔧 Component-by-Component Comparison

### API Services

| Component | Production | Demo | Change Reasoning |
|-----------|------------|------|------------------|
| **Replicas** | 3-5 per service | 1 per service | Resource constraints |
| **Memory** | 2-4GB per service | 512MB per service | Reduced concurrent users |
| **CPU** | 2-4 cores per service | 0.5-1 core per service | Lower processing demands |
| **Load Balancing** | Multiple replicas + ALB | Single pod + NodePort | No HA requirement |
| **Auto-scaling** | HPA 3-20 replicas | No auto-scaling | Fixed resource allocation |

```yaml
# Production API Service
resources:
  requests:
    memory: "2Gi"
    cpu: "1000m"
  limits:
    memory: "4Gi" 
    cpu: "2000m"
replicas: 3

# Demo API Service  
resources:
  requests:
    memory: "400Mi"
    cpu: "200m"
  limits:
    memory: "512Mi"
    cpu: "1000m"
replicas: 1
```

### Data Layer

#### PostgreSQL

| Aspect | Production | Demo | Impact |
|--------|------------|------|--------|
| **Memory** | 8-16GB | 512MB | Reduced buffer cache, connection pool |
| **Storage** | 1TB+ SSD, persistent | EmptyDir (memory) | Data lost on restart |
| **Connections** | 200-500 max | 20 max | Limited concurrent users |
| **Replication** | Master-slave setup | Single instance | No failover capability |
| **Backup** | Automated daily backups | No backups | Acceptable for demo |

```sql
-- Production PostgreSQL config
shared_buffers = 4GB              # 25% of 16GB
effective_cache_size = 12GB       # 75% of 16GB  
max_connections = 300

-- Demo PostgreSQL config
shared_buffers = 128MB            # 25% of 512MB
effective_cache_size = 256MB      # 50% of 512MB
max_connections = 20
```

#### Aerospike

| Aspect | Production | Demo | Impact |
|--------|------------|------|--------|
| **Cluster Size** | 3-6 nodes | 1 node | No high availability |
| **Memory per Node** | 32-64GB | 1GB total | Reduced cache capacity |
| **Storage** | SSD + Memory hybrid | Memory-only | Faster but no persistence |
| **Replication** | 2-3 replicas | 1 replica | Data loss risk |
| **Namespaces** | 5-10 specialized | 3 basic | Simplified data model |

```bash
# Production Aerospike namespace
namespace user_profiles {
    memory-size 16G
    storage-engine device {
        file /opt/aerospike/data/user_profiles.dat
        filesize 100G
        data-in-memory true
    }
    replication-factor 2
}

# Demo Aerospike namespace  
namespace user_profiles {
    memory-size 400M
    storage-engine memory    # No persistence
    replication-factor 1     # No redundancy
}
```

#### Kafka

| Aspect | Production | Demo | Impact |
|--------|------------|------|--------|
| **Brokers** | 3-6 brokers | 1 broker | No fault tolerance |
| **Memory per Broker** | 8-16GB | 1.2GB | Reduced throughput |
| **Partitions** | 12-24 per topic | 2 per topic | Limited parallelism |
| **Retention** | 7-30 days | 24 hours | Minimal data history |
| **Replication** | 3 replicas | 1 replica | Data loss risk |

```yaml
# Production Kafka config
KAFKA_HEAP_OPTS: "-Xmx8g -Xms8g"
KAFKA_NUM_PARTITIONS: 12
KAFKA_DEFAULT_REPLICATION_FACTOR: 3
KAFKA_LOG_RETENTION_HOURS: 168  # 7 days

# Demo Kafka config
KAFKA_HEAP_OPTS: "-Xmx800m -Xms800m" 
KAFKA_NUM_PARTITIONS: 2
KAFKA_DEFAULT_REPLICATION_FACTOR: 1
KAFKA_LOG_RETENTION_HOURS: 24  # 1 day
```

### Processing Layer

#### Apache Flink

| Aspect | Production | Demo | Impact |
|--------|------------|------|--------|
| **JobManager** | 2-3 instances (HA) | 1 instance | Single point of failure |
| **TaskManager** | 6-12 instances | 1 instance | Limited processing power |
| **Memory per TM** | 8-16GB | 1GB | Reduced state capacity |
| **Task Slots** | 8-16 per TM | 2 per TM | Limited parallelism |
| **Checkpointing** | S3/HDFS persistent | Local ephemeral | State lost on restart |

```yaml
# Production Flink TaskManager
taskmanager.memory.process.size: 8g
taskmanager.numberOfTaskSlots: 8
parallelism.default: 48
state.backend.rocksdb.memory.managed: true
state.checkpoints.dir: s3://prod-checkpoints

# Demo Flink TaskManager
taskmanager.memory.process.size: 800m
taskmanager.numberOfTaskSlots: 2  
parallelism.default: 2
state.backend.rocksdb.memory.managed: true
state.checkpoints.dir: file:///tmp/checkpoints
```

## 📈 Performance Comparison

### Throughput Expectations

| Metric | Production | Demo | Demo/Prod Ratio |
|--------|------------|------|-----------------|
| **Event Ingestion** | 50,000 events/sec | 500 events/sec | 1% |
| **API Requests** | 10,000 req/sec | 100 req/sec | 1% |
| **Rule Evaluations** | 100,000/sec | 1,000/sec | 1% |
| **Concurrent Users** | 10,000+ | 10-20 | 0.1% |
| **Data Storage** | 10TB+ | 5GB | 0.05% |

### Latency Expectations

| Operation | Production | Demo | Acceptable Degradation |
|-----------|------------|------|----------------------|
| **API Response** | < 200ms | < 500ms | 2.5x slower |
| **Event Processing** | < 100ms | < 1000ms | 10x slower |
| **Rule Evaluation** | < 50ms | < 200ms | 4x slower |
| **Cache Access** | < 5ms | < 20ms | 4x slower |
| **Database Query** | < 100ms | < 300ms | 3x slower |

## 🔄 Resource Optimization Strategies

### Memory Optimization for Demo

```yaml
# Production JVM settings
JAVA_OPTS: "-Xmx4g -Xms4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Demo JVM settings (memory-optimized)
JAVA_OPTS: "-Xmx300m -Xms300m -XX:+UseSerialGC -XX:MaxHeapFreeRatio=20"
```

### CPU Optimization for Demo

```yaml
# Production: Multiple threads/workers
server.tomcat.threads.max: 200
spring.kafka.consumer.concurrency: 8
flink.parallelism.default: 48

# Demo: Reduced concurrency
server.tomcat.threads.max: 20
spring.kafka.consumer.concurrency: 2  
flink.parallelism.default: 2
```

### Storage Optimization for Demo

```yaml
# Production: Persistent, replicated storage
spec:
  storageClassName: fast-ssd
  accessModes: [ReadWriteOnce]
  resources:
    requests:
      storage: 100Gi

# Demo: Ephemeral, in-memory storage
spec:
  volumes:
  - name: data
    emptyDir:
      sizeLimit: 1Gi
      medium: Memory  # RAM disk for speed
```

## 🚨 Trade-offs and Limitations

### Demo Limitations

| Category | Limitation | Production Mitigation |
|----------|------------|----------------------|
| **Availability** | Single point of failure | Multi-replica, cross-AZ deployment |
| **Data Loss** | No persistence | Persistent volumes, automated backups |
| **Scalability** | Fixed single-node capacity | Auto-scaling, load balancing |
| **Security** | Basic authentication only | Full TLS, RBAC, network policies |
| **Monitoring** | Limited metrics | Full observability stack |
| **Performance** | 1-10% of production throughput | Optimized for high-scale workloads |

### Acceptable Trade-offs for Demo

✅ **Data Loss on Restart**: Demo data is recreatable  
✅ **No High Availability**: Single user/developer environment  
✅ **Limited Throughput**: Sufficient for demonstration purposes  
✅ **No Security**: Isolated development environment  
✅ **Short Data Retention**: Focus on current functionality  
✅ **No Backups**: Development/testing data only  

### Unacceptable for Production

❌ **Single Node**: Creates availability bottleneck  
❌ **No Persistence**: Business data would be lost  
❌ **No Security**: Violates compliance requirements  
❌ **Limited Monitoring**: Cannot detect production issues  
❌ **No Scaling**: Cannot handle traffic growth  
❌ **Memory Limits**: Insufficient for real user loads  

## 🔄 Migration Path: Demo → Production

### Phase 1: Infrastructure Scaling
1. **Multi-node Kubernetes cluster** (minimum 3 nodes)
2. **Persistent storage** (SSD-based StorageClasses)
3. **Load balancer** (ingress controller, external LB)
4. **Monitoring stack** (full Prometheus/Grafana/ELK)

### Phase 2: Resource Scaling
1. **Memory**: Scale each component 5-10x
2. **CPU**: Add multiple cores per service
3. **Replicas**: Minimum 3 replicas per service
4. **Storage**: Persistent volumes with backup strategy

### Phase 3: Production Hardening
1. **Security**: TLS everywhere, RBAC, network policies
2. **High Availability**: Multi-AZ deployment
3. **Disaster Recovery**: Cross-region replication
4. **Compliance**: Audit logging, data governance

### Migration Command Examples

```bash
# Scale memory for production
kubectl patch deployment segment-api -n audience-manager-prod \
  -p '{"spec":{"template":{"spec":{"containers":[{"name":"segment-api","resources":{"requests":{"memory":"2Gi"},"limits":{"memory":"4Gi"}}}]}}}}'

# Scale replicas for production  
kubectl scale deployment --replicas=3 segment-api -n audience-manager-prod

# Add persistent storage
kubectl apply -f production-storage-class.yaml
kubectl apply -f production-persistent-volumes.yaml
```

## 📋 Summary

The demo architecture provides **100% functional coverage** of the production system while operating at approximately **1-10% of production scale**. This makes it ideal for:

✅ **Feature Development**: All APIs and integrations work identically  
✅ **Integration Testing**: Complete end-to-end workflows  
✅ **User Training**: Full UI and feature demonstrations  
✅ **Proof of Concept**: Validate architecture decisions  
✅ **Local Development**: Fast iteration and debugging  

The resource-optimized demo environment successfully demonstrates all core capabilities of the Audience Manager platform within the constraints of a single development machine! 🎯
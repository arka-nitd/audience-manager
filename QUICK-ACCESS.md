# 🚀 Audience Manager - Quick Access Guide

## 🌐 **Instant Web Access** (Copy & Paste)

```bash
# Open all UIs in your browser
open http://localhost:30080   # Kafka UI (Message Management)
open http://localhost:30881   # Flink Dashboard (Stream Processing) 
open http://localhost:30001   # Grafana (Monitoring)
open http://localhost:30090   # Prometheus (Metrics)
```

## 🔧 **Database Quick Connect**

```bash
# Aerospike NoSQL Database
aql -h 127.0.0.1 -p 30300

# PostgreSQL Database
psql -h localhost -p 30432 -U postgres -d audience_manager
```

## 📊 **Pod Status Check**

```bash
kubectl get pods -n audience-manager-demo -o wide
```

## 🔄 **Kafka CLI Quick Commands**

```bash
# List all topics
kubectl exec -it -n audience-manager-demo kafka-7d554f6dc7-bcf8k -- kafka-topics --list --bootstrap-server localhost:9092

# Produce test message
kubectl exec -it -n audience-manager-demo kafka-7d554f6dc7-bcf8k -- kafka-console-producer --topic user-events --bootstrap-server localhost:9092

# Consume messages
kubectl exec -it -n audience-manager-demo kafka-7d554f6dc7-bcf8k -- kafka-console-consumer --topic user-events --from-beginning --bootstrap-server localhost:9092
```

## ⚡ **Flink Job Management**

```bash
# Submit job
kubectl exec -it -n audience-manager-demo flink-jobmanager-685d7d449d-spmxf -- flink run /path/to/job.jar

# List running jobs
kubectl exec -it -n audience-manager-demo flink-jobmanager-685d7d449d-spmxf -- flink list
```

## 🗄️ **Current Infrastructure**

| Component | Pod | Status | Purpose |
|-----------|-----|--------|---------|
| **Aerospike** | `aerospike-68894b8cc6-zhw6b` | ✅ Running | NoSQL Database |
| **PostgreSQL** | `postgres-5dc4f6fd6d-9l95w` | ✅ Running | Relational Database |
| **Kafka** | `kafka-7d554f6dc7-bcf8k` | ✅ Running | Message Streaming |
| **Zookeeper** | `zookeeper-6b5799b8c6-vqqhc` | ✅ Running | Kafka Coordination |
| **Flink JobManager** | `flink-jobmanager-685d7d449d-spmxf` | ✅ Running | Stream Processing |
| **Flink TaskManager** | `flink-taskmanager-b59794d66-kqmz2` | ✅ Running | Worker Nodes |
| **Kafka UI** | `kafka-ui-7c8f9f6586-jv8w6` | ✅ Running | Kafka Management |
| **Prometheus** | `prometheus-566ffd4b4f-w6zh9` | ✅ Running | Metrics Collection |
| **Grafana** | `grafana-9c78c7f9-pzngz` | ✅ Running | Dashboards |

**Topics Created**: `user-events`, `segment-updates`, `user-segments`, `notification-requests`

---

🎯 **Everything is ready for your audience management system!**
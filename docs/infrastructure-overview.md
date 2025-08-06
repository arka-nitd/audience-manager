# 🚀 Audience Manager Infrastructure Overview

## 📊 Current Deployment Status

**Namespace**: `audience-manager-demo`  
**Total Pods**: 10 (9 Running + 1 Completed)  
**Cluster**: `lima-rancher-desktop`  

---

## 🗄️ **Database & Storage Components**

### 📋 **Aerospike NoSQL Database**
- **Pod**: `aerospike-68894b8cc6-zhw6b` ✅ Running
- **Purpose**: High-performance user profile & session storage
- **Internal IP**: `10.42.0.42`

**🔧 CLI Access:**
```bash
# Connect using local AQL client
aql -h 127.0.0.1 -p 30300

# Basic operations
aql> SELECT * FROM user_profiles.users WHERE PK='user123'
aql> INSERT INTO user_profiles.users (PK, username, email) VALUES ('user123', 'john', 'john@example.com')
aql> DELETE FROM user_profiles.users WHERE PK='user123'
```

**🌐 Network Ports:**
- `localhost:30300` - Main database port
- `localhost:31811` - Service port  
- `localhost:32296` - Fabric port
- `localhost:32542` - Info port

**💾 Namespaces:**
- `user_profiles` - User data (400MB memory)
- `cache` - Session cache (500MB memory)
- `analytics` - Analytics data (256MB memory)

---

### 🐘 **PostgreSQL Database**
- **Pod**: `postgres-5dc4f6fd6d-9l95w` ✅ Running  
- **Purpose**: Structured data, analytics, reporting
- **Internal IP**: `10.42.0.75`

**🔧 CLI Access:**
```bash
# Connect using psql
kubectl exec -it -n audience-manager-demo postgres-5dc4f6fd6d-9l95w -- psql -U postgres

# Or connect externally
psql -h localhost -p 30432 -U postgres -d audience_manager
```

**🌐 Network Access:**
- `localhost:30432` - External PostgreSQL connection

---

## 📨 **Messaging & Streaming**

### 🔄 **Apache Kafka** 
- **Main Pod**: `kafka-7d554f6dc7-bcf8k` ✅ Running
- **Setup Job**: `kafka-topics-setup-66rjc` ✅ Completed
- **Purpose**: Real-time event streaming & microservice communication
- **Internal IP**: `10.42.0.70`

**🔧 CLI Access:**
```bash
# List topics
kubectl exec -it -n audience-manager-demo kafka-7d554f6dc7-bcf8k -- kafka-topics --list --bootstrap-server localhost:9092

# Produce messages
kubectl exec -it -n audience-manager-demo kafka-7d554f6dc7-bcf8k -- kafka-console-producer --topic user-events --bootstrap-server localhost:9092

# Consume messages  
kubectl exec -it -n audience-manager-demo kafka-7d554f6dc7-bcf8k -- kafka-console-consumer --topic user-events --from-beginning --bootstrap-server localhost:9092
```

**🌐 UI Access:**
- **Kafka UI**: `http://localhost:30080` 📊 **Web Management Interface**

**📋 Pre-configured Topics:**
- `user-events` - User behavioral data
- `segment-updates` - Audience segment changes
- `user-segments` - User-to-segment mappings  
- `notification-requests` - Communication triggers

---

### 🐘 **Apache Zookeeper**
- **Pod**: `zookeeper-6b5799b8c6-vqqhc` ✅ Running
- **Purpose**: Kafka coordination & distributed configuration
- **Internal IP**: `10.42.0.61`

**🔧 CLI Access:**
```bash
# Connect to Zookeeper shell
kubectl exec -it -n audience-manager-demo zookeeper-6b5799b8c6-vqqhc -- zookeeper-shell localhost:2181

# Check Kafka brokers
kubectl exec -it -n audience-manager-demo zookeeper-6b5799b8c6-vqqhc -- zookeeper-shell localhost:2181 <<< "ls /brokers/ids"
```

---

## 🌊 **Stream Processing**

### ⚡ **Apache Flink Cluster**
- **JobManager**: `flink-jobmanager-685d7d449d-spmxf` ✅ Running
- **TaskManager**: `flink-taskmanager-b59794d66-kqmz2` ✅ Running  
- **Purpose**: Real-time stream processing & analytics
- **JobManager IP**: `10.42.0.46`
- **TaskManager IP**: `10.42.0.57`

**🌐 UI Access:**
- **Flink Web UI**: `http://localhost:30881` 📊 **Job Management Dashboard**

**🔧 CLI Access:**
```bash
# Submit a job
kubectl exec -it -n audience-manager-demo flink-jobmanager-685d7d449d-spmxf -- flink run /opt/flink/examples/streaming/WordCount.jar

# List jobs
kubectl exec -it -n audience-manager-demo flink-jobmanager-685d7d449d-spmxf -- flink list

# Job management
kubectl exec -it -n audience-manager-demo flink-jobmanager-685d7d449d-spmxf -- flink cancel <job-id>
```

**💡 Capabilities:**
- Real-time user segmentation
- Event stream processing  
- Complex event pattern detection
- Stateful computations

---

## 📊 **Monitoring & Observability**

### 📈 **Prometheus Metrics**
- **Pod**: `prometheus-566ffd4b4f-w6zh9` ✅ Running
- **Purpose**: Metrics collection & time-series storage
- **Internal IP**: `10.42.0.72`

**🌐 UI Access:**
- **Prometheus UI**: `http://localhost:30090` 📊 **Metrics Query Interface**

**🔧 CLI Access:**
```bash
# Query metrics via API
curl "http://localhost:30090/api/v1/query?query=up"

# Check targets
curl "http://localhost:30090/api/v1/targets"
```

---

### 📊 **Grafana Dashboards**
- **Pod**: `grafana-9c78c7f9-pzngz` ✅ Running
- **Purpose**: Visualization & alerting dashboards
- **Internal IP**: `10.42.0.73`

**🌐 UI Access:**
- **Grafana UI**: `http://localhost:30001` 📊 **Visualization Dashboards**

**🔑 Default Credentials:**
- Username: `admin`
- Password: `admin` (change on first login)

---

## 🎯 **Quick Access Summary**

### 🌐 **Web Interfaces** (Primary Access Method)
| Service | URL | Status | Purpose |
|---------|-----|--------|---------|
| **Kafka UI** | `http://localhost:30080` | ✅ Ready | Message management |
| **Flink Dashboard** | `http://localhost:30881` | ✅ Ready | Stream processing |
| **Grafana** | `http://localhost:30001` | ✅ Ready | Monitoring dashboards |
| **Prometheus** | `http://localhost:30090` | ✅ Ready | Metrics queries |

### 🔧 **CLI Database Connections**
```bash
# Aerospike
aql -h 127.0.0.1 -p 30300

# PostgreSQL  
psql -h localhost -p 30432 -U postgres -d audience_manager
```

### 🐳 **Pod Shell Access**
```bash
# Generic pod access
kubectl exec -it -n audience-manager-demo <pod-name> -- /bin/bash

# Examples:
kubectl exec -it -n audience-manager-demo kafka-7d554f6dc7-bcf8k -- /bin/bash
kubectl exec -it -n audience-manager-demo postgres-5dc4f6fd6d-9l95w -- /bin/bash
```

---

## 🏗️ **Architecture Flow**

```
📊 Data Ingestion → 🔄 Kafka Topics → ⚡ Flink Processing → 🗄️ Storage (Aerospike/PostgreSQL)
                                                              ↓
📈 Prometheus Metrics ← 📊 Grafana Dashboards ← 🔍 Monitoring & Alerting
```

---

## 🚀 **Next Steps**

1. **Deploy Application Components:**
   - `audience-manager-ingestion`
   - `audience-manager-processor`  
   - `audience-manager-communication`
   - `audience-manager-api`

2. **Configure Data Flows:**
   - Set up Flink jobs for real-time processing
   - Configure Prometheus scraping for app metrics
   - Create Grafana dashboards for business metrics

3. **Production Readiness:**
   - Enable persistent storage
   - Configure resource limits
   - Set up backup strategies
   - Implement security policies

---

**🎊 Your audience management infrastructure is fully operational and ready for application deployment!**
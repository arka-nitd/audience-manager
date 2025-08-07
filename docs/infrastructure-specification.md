# Infrastructure Specification - Audience Manager Platform

## 1. Overview

This document provides comprehensive details of the Kubernetes infrastructure for the Audience Manager Platform. The infrastructure is designed to support both development/demo environments and production-ready distributed deployments.

## 2. Kubernetes Architecture

### 2.1 Cluster Configuration

#### Development/Demo Environment
- **Platform**: Rancher Desktop with Kubernetes
- **Node Configuration**: Single-node cluster
- **Resource Allocation**: 8GB RAM, 4 CPU cores
- **Namespace**: `audience-manager-demo`

#### Production Environment
- **Platform**: AWS EKS, Google GKE, or Azure AKS
- **Node Configuration**: Multi-node cluster (3+ nodes)
- **Resource Allocation**: Auto-scaling based on demand
- **Namespaces**: 
  - `audience-manager-prod`
  - `audience-manager-staging`
  - `monitoring`

### 2.2 Network Configuration

#### Service Discovery
- **DNS**: Kubernetes native DNS (CoreDNS)
- **Service Mesh**: Istio (for production)
- **Load Balancing**: Kubernetes Services + Ingress

#### Port Mapping (Development)
```yaml
Service Ports:
  - Audience Manager API: 30280 (HTTP)
  - Audience Manager UI: 30280 (HTTP via proxy)
  - PostgreSQL: 30432
  - Aerospike: 30300
  - Kafka: 30092
  - Zookeeper: 32181
  - Flink JobManager: 30881
  - Flink UI: 30881
  - Prometheus: 30090
  - Grafana: 30001
  - Kafka UI: 30080
```

## 3. Application Services

### 3.1 Audience Manager API

**Deployment Specification:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: audience-manager-api
  namespace: audience-manager-demo
spec:
  replicas: 1  # Scale to 3+ in production
  selector:
    matchLabels:
      app: audience-manager-api
  template:
    metadata:
      labels:
        app: audience-manager-api
    spec:
      containers:
      - name: api
        image: audience-manager-api:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DATABASE_URL
          value: "jdbc:postgresql://postgres-service:5432/audience_manager"
        resources:
          requests:
            memory: "256Mi"
            cpu: "200m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

**Service Configuration:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: audience-manager-api-service
  namespace: audience-manager-demo
spec:
  selector:
    app: audience-manager-api
  ports:
  - name: http
    port: 8080
    targetPort: 8080
    nodePort: 30280
  type: NodePort
```

**Resource Requirements:**
- **Development**: 256Mi RAM, 200m CPU
- **Production**: 1Gi RAM, 500m CPU (per replica)
- **Scaling**: HPA based on CPU (70%) and memory (80%)

### 3.2 Audience Manager UI

**Deployment Specification:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: audience-manager-ui
  namespace: audience-manager-demo
spec:
  replicas: 1  # Scale to 2+ in production
  selector:
    matchLabels:
      app: audience-manager-ui
  template:
    metadata:
      labels:
        app: audience-manager-ui
    spec:
      containers:
      - name: ui
        image: audience-manager-ui:latest
        ports:
        - containerPort: 80
        resources:
          requests:
            memory: "64Mi"
            cpu: "50m"
          limits:
            memory: "128Mi"
            cpu: "100m"
        livenessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 5
```

**Nginx Configuration:**
```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # Serve React app
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API requests
    location /api/ {
        proxy_pass http://audience-manager-api-service:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Health check
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
```

## 4. Data Services

### 4.1 PostgreSQL Database

**Deployment Specification:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: audience-manager-demo
spec:
  replicas: 1  # Use StatefulSet for production
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:15-alpine
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_DB
          value: "audience_manager"
        - name: POSTGRES_USER
          value: "postgres"
        - name: POSTGRES_PASSWORD
          value: "demo123"  # Use secrets in production
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
        resources:
          requests:
            memory: "256Mi"
            cpu: "200m"
          limits:
            memory: "512Mi"
            cpu: "500m"
      volumes:
      - name: postgres-storage
        emptyDir: {}  # Use PVC in production
```

**Production Considerations:**
- Use StatefulSet for stable storage
- Implement PersistentVolumeClaims
- Configure backup strategies
- Set up read replicas for scaling
- Use secrets for credentials

### 4.2 Aerospike NoSQL Database

**Deployment Specification:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: aerospike
  namespace: audience-manager-demo
spec:
  replicas: 1  # Scale to 3+ nodes in production
  selector:
    matchLabels:
      app: aerospike
  template:
    metadata:
      labels:
        app: aerospike
    spec:
      containers:
      - name: aerospike
        image: aerospike:ce-6.4.0.10
        ports:
        - containerPort: 3000
        - containerPort: 3001
        - containerPort: 3002
        - containerPort: 3003
        volumeMounts:
        - name: aerospike-config
          mountPath: /opt/aerospike/etc/aerospike.conf
          subPath: aerospike.conf
        resources:
          requests:
            memory: "512Mi"
            cpu: "300m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: aerospike-config
        configMap:
          name: aerospike-config
```

**Configuration (ConfigMap):**
```hocon
service {
    user root
    group root
    paxos-single-replica-limit 1
    pidfile /var/run/aerospike/asd.pid
    service-threads 4
    transaction-queues 4
    transaction-threads-per-queue 4
    proto-fd-max 15000
}

logging {
    file /var/log/aerospike/aerospike.log {
        context any info
    }
}

network {
    service {
        address any
        port 3000
    }
    heartbeat {
        mode mesh
        port 3002
        address any
        interval 150
        timeout 10
    }
    fabric {
        port 3001
        address any
    }
    info {
        port 3003
        address any
    }
}

namespace user_profiles {
    replication-factor 1
    memory-size 400M
    storage-engine memory
}

namespace cache {
    replication-factor 1
    memory-size 500M
    storage-engine memory
}

namespace analytics {
    replication-factor 1
    memory-size 256M
    storage-engine memory
}
```

## 5. Messaging and Stream Processing

### 5.1 Apache Kafka

**Zookeeper Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zookeeper
  namespace: audience-manager-demo
spec:
  replicas: 1  # Scale to 3+ in production
  selector:
    matchLabels:
      app: zookeeper
  template:
    metadata:
      labels:
        app: zookeeper
    spec:
      containers:
      - name: zookeeper
        image: confluentinc/cp-zookeeper:7.4.0
        ports:
        - containerPort: 2181
        env:
        - name: ZOOKEEPER_CLIENT_PORT
          value: "2181"
        - name: ZOOKEEPER_TICK_TIME
          value: "2000"
        resources:
          requests:
            memory: "256Mi"
            cpu: "200m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

**Kafka Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka
  namespace: audience-manager-demo
spec:
  replicas: 1  # Scale to 3+ in production
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
      - name: kafka
        image: confluentinc/cp-kafka:7.4.0
        ports:
        - containerPort: 9092
        - containerPort: 9101
        env:
        - name: KAFKA_BROKER_ID
          value: "1"
        - name: KAFKA_ZOOKEEPER_CONNECT
          value: "zookeeper-service:2181"
        - name: KAFKA_LISTENER_SECURITY_PROTOCOL_MAP
          value: "PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT"
        - name: KAFKA_ADVERTISED_LISTENERS
          value: "PLAINTEXT://kafka-service:9092,PLAINTEXT_HOST://localhost:30092"
        - name: KAFKA_LISTENERS
          value: "PLAINTEXT://0.0.0.0:9092,PLAINTEXT_HOST://0.0.0.0:29092"
        - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
          value: "1"
        - name: KAFKA_TRANSACTION_STATE_LOG_MIN_ISR
          value: "1"
        - name: KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR
          value: "1"
        - name: KAFKA_HEAP_OPTS
          value: "-Xmx512m -Xms512m"
        resources:
          requests:
            memory: "512Mi"
            cpu: "300m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

**Topic Creation Job:**
```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: kafka-topics-setup
  namespace: audience-manager-demo
spec:
  template:
    spec:
      containers:
      - name: kafka-topics
        image: confluentinc/cp-kafka:7.4.0
        command:
        - /bin/bash
        - -c
        - |
          # Wait for Kafka to be ready
          while ! nc -z kafka-service 9092; do sleep 1; done
          
          # Create topics
          kafka-topics --create --topic user-events --bootstrap-server kafka-service:9092 --partitions 3 --replication-factor 1 || true
          kafka-topics --create --topic segment-updates --bootstrap-server kafka-service:9092 --partitions 3 --replication-factor 1 || true
          kafka-topics --create --topic segment-memberships --bootstrap-server kafka-service:9092 --partitions 6 --replication-factor 1 || true
          kafka-topics --create --topic campaign-triggers --bootstrap-server kafka-service:9092 --partitions 3 --replication-factor 1 || true
      restartPolicy: OnFailure
  backoffLimit: 3
```

### 5.2 Apache Flink

**JobManager Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flink-jobmanager
  namespace: audience-manager-demo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: flink-jobmanager
  template:
    metadata:
      labels:
        app: flink-jobmanager
    spec:
      containers:
      - name: jobmanager
        image: flink:1.18.0-scala_2.12-java11
        args: ["jobmanager"]
        ports:
        - containerPort: 6123
        - containerPort: 8081
        env:
        - name: FLINK_PROPERTIES
          value: |
            jobmanager.rpc.address: flink-jobmanager-service
            jobmanager.memory.process.size: 1024m
            taskmanager.memory.process.size: 1024m
            taskmanager.numberOfTaskSlots: 2
        resources:
          requests:
            memory: "512Mi"
            cpu: "300m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

**TaskManager Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flink-taskmanager
  namespace: audience-manager-demo
spec:
  replicas: 1  # Scale to 2+ in production
  selector:
    matchLabels:
      app: flink-taskmanager
  template:
    metadata:
      labels:
        app: flink-taskmanager
    spec:
      containers:
      - name: taskmanager
        image: flink:1.18.0-scala_2.12-java11
        args: ["taskmanager"]
        env:
        - name: FLINK_PROPERTIES
          value: |
            jobmanager.rpc.address: flink-jobmanager-service
            taskmanager.numberOfTaskSlots: 2
            taskmanager.memory.process.size: 1024m
        resources:
          requests:
            memory: "512Mi"
            cpu: "300m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

## 6. Monitoring and Observability

### 6.1 Prometheus

**Deployment Specification:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: audience-manager-demo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      containers:
      - name: prometheus
        image: prom/prometheus:v2.45.0
        ports:
        - containerPort: 9090
        args:
        - '--config.file=/etc/prometheus/prometheus.yml'
        - '--storage.tsdb.path=/prometheus/'
        - '--web.console.libraries=/etc/prometheus/console_libraries'
        - '--web.console.templates=/etc/prometheus/consoles'
        - '--storage.tsdb.retention.time=200h'
        - '--web.enable-lifecycle'
        volumeMounts:
        - name: prometheus-config
          mountPath: /etc/prometheus/
        - name: prometheus-storage
          mountPath: /prometheus/
        resources:
          requests:
            memory: "256Mi"
            cpu: "200m"
          limits:
            memory: "512Mi"
            cpu: "500m"
      volumes:
      - name: prometheus-config
        configMap:
          name: prometheus-config
      - name: prometheus-storage
        emptyDir: {}
```

**Prometheus Configuration:**
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'audience-manager-api'
    static_configs:
      - targets: ['audience-manager-api-service:8080']
    metrics_path: '/actuator/prometheus'

  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-service:5432']

  - job_name: 'kafka'
    static_configs:
      - targets: ['kafka-service:9101']

  - job_name: 'flink-jobmanager'
    static_configs:
      - targets: ['flink-jobmanager-service:8081']

  - job_name: 'kubernetes-apiservers'
    kubernetes_sd_configs:
    - role: endpoints
    scheme: https
    tls_config:
      ca_file: /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
    bearer_token_file: /var/run/secrets/kubernetes.io/serviceaccount/token
    relabel_configs:
    - source_labels: [__meta_kubernetes_namespace, __meta_kubernetes_service_name, __meta_kubernetes_endpoint_port_name]
      action: keep
      regex: default;kubernetes;https

  - job_name: 'kubernetes-nodes'
    kubernetes_sd_configs:
    - role: node
    relabel_configs:
    - action: labelmap
      regex: __meta_kubernetes_node_label_(.+)
    - target_label: __address__
      replacement: kubernetes.default.svc:443
    - source_labels: [__meta_kubernetes_node_name]
      regex: (.+)
      target_label: __metrics_path__
      replacement: /api/v1/nodes/${1}/proxy/metrics

  - job_name: 'kubernetes-pods'
    kubernetes_sd_configs:
    - role: pod
    relabel_configs:
    - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
      action: keep
      regex: true
    - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
      action: replace
      target_label: __metrics_path__
      regex: (.+)
    - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
      action: replace
      regex: ([^:]+)(?::\d+)?;(\d+)
      replacement: $1:$2
      target_label: __address__
    - action: labelmap
      regex: __meta_kubernetes_pod_label_(.+)
    - source_labels: [__meta_kubernetes_namespace]
      action: replace
      target_label: kubernetes_namespace
    - source_labels: [__meta_kubernetes_pod_name]
      action: replace
      target_label: kubernetes_pod_name
```

### 6.2 Grafana

**Deployment Specification:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: audience-manager-demo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
    spec:
      containers:
      - name: grafana
        image: grafana/grafana:10.0.0
        ports:
        - containerPort: 3000
        env:
        - name: GF_SECURITY_ADMIN_PASSWORD
          value: "admin"  # Use secrets in production
        - name: GF_INSTALL_PLUGINS
          value: "grafana-kubernetes-app"
        volumeMounts:
        - name: grafana-storage
          mountPath: /var/lib/grafana
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
      volumes:
      - name: grafana-storage
        emptyDir: {}
```

## 7. Utility Services

### 7.1 Kafka UI

**Deployment Specification:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka-ui
  namespace: audience-manager-demo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka-ui
  template:
    metadata:
      labels:
        app: kafka-ui
    spec:
      containers:
      - name: kafka-ui
        image: provectuslabs/kafka-ui:latest
        ports:
        - containerPort: 8080
        env:
        - name: KAFKA_CLUSTERS_0_NAME
          value: "local"
        - name: KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS
          value: "kafka-service:9092"
        - name: KAFKA_CLUSTERS_0_ZOOKEEPER
          value: "zookeeper-service:2181"
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
```

## 8. Security Configuration

### 8.1 RBAC (Role-Based Access Control)

**ServiceAccount:**
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: audience-manager-sa
  namespace: audience-manager-demo
```

**ClusterRole:**
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: audience-manager-role
rules:
- apiGroups: [""]
  resources: ["pods", "services", "endpoints"]
  verbs: ["get", "list", "watch"]
- apiGroups: ["apps"]
  resources: ["deployments", "replicasets"]
  verbs: ["get", "list", "watch"]
```

**ClusterRoleBinding:**
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: audience-manager-binding
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: audience-manager-role
subjects:
- kind: ServiceAccount
  name: audience-manager-sa
  namespace: audience-manager-demo
```

### 8.2 Network Policies

**Default Deny Policy:**
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
  namespace: audience-manager-demo
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
```

**API Service Policy:**
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: api-network-policy
  namespace: audience-manager-demo
spec:
  podSelector:
    matchLabels:
      app: audience-manager-api
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: audience-manager-ui
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: postgres
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - podSelector:
        matchLabels:
          app: kafka
    ports:
    - protocol: TCP
      port: 9092
```

## 9. Resource Management

### 9.1 Resource Quotas

**Namespace Quota:**
```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: audience-manager-quota
  namespace: audience-manager-demo
spec:
  hard:
    requests.cpu: "4"
    requests.memory: 8Gi
    limits.cpu: "8"
    limits.memory: 16Gi
    persistentvolumeclaims: "10"
    pods: "20"
    services: "15"
```

### 9.2 Limit Ranges

**Default Limits:**
```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: audience-manager-limits
  namespace: audience-manager-demo
spec:
  limits:
  - default:
      cpu: "500m"
      memory: "512Mi"
    defaultRequest:
      cpu: "100m"
      memory: "128Mi"
    type: Container
```

### 9.3 Horizontal Pod Autoscaler (Production)

**API Service HPA:**
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: audience-manager-api-hpa
  namespace: audience-manager-demo
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: audience-manager-api
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

## 10. Persistent Storage

### 10.1 Storage Classes

**Development (Local Storage):**
```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: local-storage
provisioner: kubernetes.io/no-provisioner
volumeBindingMode: WaitForFirstConsumer
```

**Production (Cloud Storage):**
```yaml
# AWS EBS
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: gp3-storage
provisioner: ebs.csi.aws.com
parameters:
  type: gp3
  fsType: ext4
  encrypted: "true"
volumeBindingMode: WaitForFirstConsumer
allowVolumeExpansion: true
```

### 10.2 Persistent Volume Claims

**PostgreSQL PVC:**
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: audience-manager-demo
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: gp3-storage
  resources:
    requests:
      storage: 100Gi
```

**Aerospike PVC:**
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: aerospike-pvc
  namespace: audience-manager-demo
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: gp3-storage
  resources:
    requests:
      storage: 200Gi
```

## 11. Backup and Disaster Recovery

### 11.1 Backup Strategies

**Database Backups:**
```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
  namespace: audience-manager-demo
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: postgres-backup
            image: postgres:15-alpine
            command:
            - /bin/bash
            - -c
            - |
              pg_dump -h postgres-service -U postgres -d audience_manager > /backup/backup-$(date +%Y%m%d).sql
              # Upload to S3 or other storage
            env:
            - name: PGPASSWORD
              value: "demo123"
            volumeMounts:
            - name: backup-storage
              mountPath: /backup
          volumes:
          - name: backup-storage
            persistentVolumeClaim:
              claimName: backup-pvc
          restartPolicy: OnFailure
```

### 11.2 Disaster Recovery Procedures

**Configuration Backup:**
```bash
# Backup all Kubernetes configurations
kubectl get all --namespace=audience-manager-demo -o yaml > backup-configs.yaml

# Backup secrets and configmaps
kubectl get secrets,configmaps --namespace=audience-manager-demo -o yaml > backup-secrets.yaml

# Backup persistent volumes
kubectl get pv,pvc --namespace=audience-manager-demo -o yaml > backup-storage.yaml
```

## 12. Health Checks and Monitoring

### 12.1 Readiness and Liveness Probes

**Application Health Checks:**
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30
  timeoutSeconds: 10
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### 12.2 Custom Metrics

**Application Metrics (Spring Boot Actuator):**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus,metrics,info
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: audience-manager-api
      environment: demo
```

## 13. Deployment Pipeline

### 13.1 CI/CD Integration

**GitHub Actions Workflow:**
```yaml
name: Deploy to Kubernetes
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Build API
      run: |
        cd audience-manager-api
        ./mvnw clean package -DskipTests
    
    - name: Build UI
      run: |
        cd audience-manager-ui
        npm ci
        npm run build
    
    - name: Build Docker Images
      run: |
        docker build -t audience-manager-api:${{ github.sha }} audience-manager-api/
        docker build -t audience-manager-ui:${{ github.sha }} audience-manager-ui/
    
    - name: Deploy to Kubernetes
      run: |
        kubectl apply -f kube-config/deployment-specs/
        kubectl set image deployment/audience-manager-api api=audience-manager-api:${{ github.sha }} -n audience-manager-demo
        kubectl set image deployment/audience-manager-ui ui=audience-manager-ui:${{ github.sha }} -n audience-manager-demo
```

### 13.2 Blue-Green Deployment

**Blue-Green Service:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: audience-manager-api-blue-green
  namespace: audience-manager-demo
spec:
  selector:
    app: audience-manager-api
    version: blue  # Switch to green during deployment
  ports:
  - port: 8080
    targetPort: 8080
```

## 14. Troubleshooting and Operations

### 14.1 Common Operations

**Scaling Services:**
```bash
# Scale API service
kubectl scale deployment audience-manager-api --replicas=3 -n audience-manager-demo

# Scale TaskManager
kubectl scale deployment flink-taskmanager --replicas=2 -n audience-manager-demo
```

**Rolling Updates:**
```bash
# Update API image
kubectl set image deployment/audience-manager-api api=audience-manager-api:v2.0.0 -n audience-manager-demo

# Check rollout status
kubectl rollout status deployment/audience-manager-api -n audience-manager-demo

# Rollback if needed
kubectl rollout undo deployment/audience-manager-api -n audience-manager-demo
```

**Debug Pods:**
```bash
# Get pod logs
kubectl logs -f audience-manager-api-xxx -n audience-manager-demo

# Execute into pod
kubectl exec -it audience-manager-api-xxx -n audience-manager-demo -- /bin/bash

# Port forward for debugging
kubectl port-forward service/audience-manager-api-service 8080:8080 -n audience-manager-demo
```

### 14.2 Performance Tuning

**JVM Tuning for API Service:**
```yaml
env:
- name: JAVA_OPTS
  value: "-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport"
```

**Kafka Performance:**
```yaml
env:
- name: KAFKA_HEAP_OPTS
  value: "-Xmx512m -Xms512m"
- name: KAFKA_JVM_PERFORMANCE_OPTS
  value: "-XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35"
```

## 15. Cost Optimization

### 15.1 Resource Right-Sizing

**Development Environment:**
- Total allocation: ~6GB RAM, 3 CPU cores
- Cost: ~$50-100/month (cloud equivalent)

**Production Environment:**
- Recommended: 3 nodes x 4 vCPU x 16GB RAM
- Auto-scaling: 6-12 nodes during peak
- Cost: ~$500-1500/month (depending on cloud provider)

### 15.2 Optimization Strategies

- Use spot instances for non-critical workloads
- Implement cluster autoscaler
- Use reserved instances for predictable workloads
- Implement pod disruption budgets
- Use namespace-based resource quotas

---

This infrastructure specification provides a comprehensive guide for deploying and managing the Audience Manager Platform on Kubernetes, supporting both development and production environments with appropriate scaling, monitoring, and operational considerations.
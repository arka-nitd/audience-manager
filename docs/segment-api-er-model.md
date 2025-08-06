# Segment API - Entity Relationship Model

## Overview
The Segment API supports two types of segments:
1. **Independent Segments**: Based on attribute + operator + value over a time window
2. **Derived Segments**: Combination of independent segments using logical operators

## Entity Relationship Diagram

```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│    segments     │      │   segment_rules  │      │ segment_dependencies │
├─────────────────┤      ├──────────────────┤      ├─────────────────┤
│ id (PK)         │◄────┐│ id (PK)          │      │ id (PK)         │
│ name            │     ││ segment_id (FK)  │      │ derived_segment_id (FK) │
│ description     │     │├──────────────────┤      │ independent_segment_id (FK) │
│ type            │     ││ event_type       │      │ logical_operator │
│ segment_type    │     ││ attribute        │      │ created_at      │
│ logical_expression │   ││ operator         │      └─────────────────┘
│ window_minutes  │     ││ value            │
│ active          │     ││ window_minutes   │
│ created_at      │     ││ active           │
│ updated_at      │     ││ created_at       │
└─────────────────┘     │└──────────────────┘
                        │
                        └──── Foreign Key Relationship
```

## Database Schema

### 1. segments
Primary table storing all segments (both independent and derived)

```sql
CREATE TABLE segments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INDEPENDENT', 'DERIVED')),
    segment_type VARCHAR(20) NOT NULL CHECK (segment_type IN ('STATIC', 'DYNAMIC', 'COMPUTED', 'LOOKALIKE')),
    logical_expression TEXT, -- Used for derived segments: "segment_1 AND segment_2"
    window_minutes INTEGER DEFAULT 5, -- Time window for evaluation
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. segment_rules
Rules for independent segments (attribute-operator-value conditions)

```sql
CREATE TABLE segment_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    segment_id UUID NOT NULL REFERENCES segments(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL CHECK (event_type IN ('clicks', 'installs', 'orders', 'addToCart')),
    attribute VARCHAR(100) NOT NULL, -- e.g., 'count', 'sum', 'avg'
    operator VARCHAR(20) NOT NULL CHECK (operator IN ('>', '<', '>=', '<=', '=', '!=')),
    value DECIMAL(19,4) NOT NULL,
    window_minutes INTEGER NOT NULL DEFAULT 5,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3. segment_dependencies
Tracks which independent segments are used in derived segments

```sql
CREATE TABLE segment_dependencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    derived_segment_id UUID NOT NULL REFERENCES segments(id) ON DELETE CASCADE,
    independent_segment_id UUID NOT NULL REFERENCES segments(id) ON DELETE CASCADE,
    logical_operator VARCHAR(10) NOT NULL CHECK (logical_operator IN ('AND', 'OR', 'NOT')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(derived_segment_id, independent_segment_id)
);
```

## Indexes for Performance

```sql
-- Performance indexes
CREATE INDEX idx_segments_type ON segments(type);
CREATE INDEX idx_segments_active ON segments(active);
CREATE INDEX idx_segment_rules_segment_id ON segment_rules(segment_id);
CREATE INDEX idx_segment_rules_event_type ON segment_rules(event_type);
CREATE INDEX idx_segment_dependencies_derived ON segment_dependencies(derived_segment_id);
CREATE INDEX idx_segment_dependencies_independent ON segment_dependencies(independent_segment_id);
```

## Example Data

### Independent Segment Example
```json
{
  "name": "High Clickers",
  "description": "Users with more than 100 clicks in last 5 minutes",
  "type": "INDEPENDENT",
  "segment_type": "DYNAMIC",
  "window_minutes": 5,
  "rules": [
    {
      "event_type": "clicks",
      "attribute": "count",
      "operator": ">",
      "value": 100,
      "window_minutes": 5
    }
  ]
}
```

### Derived Segment Example
```json
{
  "name": "High Value Users",
  "description": "High clickers AND high installers",
  "type": "DERIVED",
  "segment_type": "DYNAMIC",
  "logical_expression": "segment_1 AND segment_2",
  "dependencies": [
    {
      "independent_segment_id": "uuid-of-high-clickers",
      "logical_operator": "AND"
    },
    {
      "independent_segment_id": "uuid-of-high-installers",
      "logical_operator": "AND"
    }
  ]
}
```

## API Endpoints

### Create Segment
- **POST** `/api/v1/segments`
- **Input**: Segment creation request (independent or derived)
- **Output**: Created segment with unique ID

### Get Segment
- **GET** `/api/v1/segments/{id}`
- **Output**: Full segment details with rules/dependencies

### List Segments
- **GET** `/api/v1/segments`
- **Query Parameters**: `type`, `active`, `page`, `size`
- **Output**: Paginated list of segments

### Update Segment
- **PUT** `/api/v1/segments/{id}`
- **Input**: Updated segment data
- **Output**: Updated segment

### Delete Segment
- **DELETE** `/api/v1/segments/{id}`
- **Output**: Success confirmation

### Activate/Deactivate Segment
- **PATCH** `/api/v1/segments/{id}/status`
- **Input**: `{"active": true/false}`
- **Output**: Updated segment

## Validation Rules

1. **Event Types**: Must be one of `clicks`, `installs`, `orders`, `addToCart`
2. **Operators**: Must be one of `>`, `<`, `>=`, `<=`, `=`, `!=`
3. **Logical Operators**: Must be one of `AND`, `OR`, `NOT`
4. **Window Minutes**: Must be positive integer (1-1440 minutes)
5. **Segment Names**: Must be unique across the system
6. **Derived Segments**: Must reference at least 2 independent segments
7. **Circular Dependencies**: Derived segments cannot reference other derived segments

## Business Logic

1. **Independent Segments**: Evaluated based on single rule conditions
2. **Derived Segments**: Evaluated by combining results of independent segments
3. **Time Windows**: All evaluations respect the specified time window
4. **Real-time Processing**: Segment membership updates sent to Kafka topics
5. **Audit Trail**: All changes tracked with timestamps
# Drift Project Architecture Explanation

## Overview
The Drift project is a **Spring Boot batch processing application** that synchronizes data from an external RedOak API system into a local database. It implements a comprehensive ETL (Extract, Transform, Load) pipeline using Spring Batch framework to handle large-scale data migration with fault tolerance and transaction management.

## Package Structure & Responsibilities

### 1. **BATCH Package** - The Processing Engine
**Location**: `com.edwardjones.drift.batch`
**Role**: Orchestrates the entire data processing pipeline

#### Core Components:

**🔧 JobConfig.java** - The Master Configuration
- **Purpose**: Defines the complete batch job structure and execution flow
- **What it does under the hood**:
  - Creates 4 separate processing steps: `loadUsers`, `loadGroups`, `loadVPs`, `loadSubmissionTypes`
  - Configures chunk-based processing (1000 users, 500 groups, 250 VPs, 500 submission types per transaction)
  - Sets up fault tolerance with retry (3 attempts) and skip (10 failures) mechanisms
  - Defines execution order to handle dependencies: Users → SubmissionTypes → Groups → VisibilityProfiles

**Example Flow**:
```
Job: redoak-import
├── Step 1: loadUsers (chunk size: 1000)
├── Step 2: loadSubmissionTypes (chunk size: 500)  
├── Step 3: loadGroups (chunk size: 500)
└── Step 4: loadVPs (chunk size: 250)
```

**🔧 RedOakStreamReader.java** - The Data Extractor
- **Purpose**: Streams JSON data from external RedOak API endpoints
- **Low-level mechanics**:
  - Establishes HTTP connection with Bearer token authentication
  - Parses JSON response as a stream (not loading entire response into memory)
  - Uses Jackson JsonParser to read JSON array element by element
  - Implements ItemStreamReader interface for Spring Batch integration

**Hypothetical Example**:
```java
// Reading from https://api.redoak.example.com/api/v1/users
// Stream processes: [user1, user2, user3, ...] one at a time
// Memory usage: O(1) per item, not O(n) for entire dataset
```

**🔧 Processors (UserProcessor, GroupProcessor, etc.)** - The Data Transformers
- **Purpose**: Convert external JSON DTOs to internal domain entities
- **Under the hood**:
  - Map JSON fields to JPA entity properties
  - Handle data type conversions (List → Set, flattening nested structures)
  - Resolve entity relationships using repository lookups
  - Apply business logic transformations

**Example Transformation**:
```java
// Input: UserJson with roles: ["ADMIN", "USER"]
// Output: User entity with roles: Set<String>{"ADMIN", "USER"}
// Also resolves: visibilityProfile lookup from database
```

### 2. **DOMAIN Package** - The Data Models
**Location**: `com.edwardjones.drift.domain`
**Role**: Defines the core business entities and their relationships

#### Entity Relationships:
```
User (1) ←→ (1) VisibilityProfile
  ↓
  (M) ←→ (M) Group
  
VisibilityProfile (1) ←→ (M) SubmissionType
VisibilityProfile (1) ←→ (M) VpGroupLink
```

**🏗️ User.java** - The Core Entity
- **Database mapping**: `users` table with separate tables for collections
- **Relationships**:
  - `user_roles` table for roles collection
  - `user_admin_roles` table for admin roles
  - `user_admin_role_permissions` table for flattened permissions
  - Many-to-many with Groups via `group_users` join table
  - Many-to-one with VisibilityProfile

**🏗️ Group.java** - The Organization Entity
- **Purpose**: Represents organizational units that contain users
- **Relationship handling**: Manages bidirectional many-to-many with Users

**🏗️ VisibilityProfile.java** - The Permission Entity
- **Purpose**: Defines what users can see and do in the system
- **Complex relationships**: Links to users, groups, and submission types through various association tables

### 3. **DTO Package** - The Data Transfer Objects
**Location**: `com.edwardjones.drift.dto`
**Role**: Represents external API response structures

#### Key Characteristics:
- **Immutable records** for thread safety
- **@JsonIgnoreProperties(ignoreUnknown = true)** for API evolution tolerance
- **Nested structures** to match external API format

**Example UserJson Structure**:
```json
{
  "userName": "john.doe",
  "firstName": "John",
  "roles": ["ADMIN", "USER"],
  "groups": [
    {"groupName": "Finance", "active": true},
    {"groupName": "IT", "active": false}
  ],
  "adminRolePermissions": [
    ["ADMIN", "CREATE_USER"],
    ["ADMIN", "DELETE_USER"]
  ]
}
```

### 4. **INFRA Package** - The Infrastructure Layer
**Location**: `com.edwardjones.drift.infra`
**Role**: Provides cross-cutting infrastructure services

**🔧 TokenService.java** - The Authentication Manager
- **Purpose**: Manages OAuth/Bearer token lifecycle for API access
- **Sophisticated caching mechanism**:
  - Thread-safe token caching with expiration
  - Double-checked locking pattern for performance
  - Automatic token refresh before expiration (90% of lifetime)
  - Exponential backoff retry logic for failures

**Under the hood example**:
```java
// First call: Fetches token, caches for 54 minutes (90% of 1 hour)
// Subsequent calls: Returns cached token instantly
// At 54 minutes: Automatically refreshes token transparently
```

**🔧 HttpConfig.java** - The HTTP Client Configuration
- **Purpose**: Configures RestTemplate with proper timeouts, SSL, and connection pooling

### 5. **REPO Package** - The Data Access Layer
**Location**: `com.edwardjones.drift.repo`
**Role**: Provides database access through Spring Data JPA

#### Repository Pattern Implementation:
- **UserRepository, GroupRepository, etc.**: Extend JpaRepository
- **Automatic query generation**: Spring Data generates SQL from method names
- **Transaction management**: Integrated with Spring's @Transactional

## Complete Processing Flow

### Phase 1: Initialization
1. **Spring Boot starts** → DriftApplication.main()
2. **Batch configuration loads** → JobConfig beans instantiated
3. **Infrastructure services initialize** → TokenService, RestTemplate configured
4. **Database connection established** → H2/PostgreSQL connection pool ready

### Phase 2: Job Execution
```
┌─────────────────────────────────────────────────────────────────┐
│                        BATCH JOB EXECUTION                     │
├─────────────────────────────────────────────────────────────────┤
│ Step 1: Load Users                                             │
│ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐    │
│ │   UserReader    │→│  UserProcessor  │→│   JpaWriter     │    │
│ │ (RedOakStream)  │ │ (JSON→Domain)   │ │ (Save to DB)    │    │
│ └─────────────────┘ └─────────────────┘ └─────────────────┘    │
│                                                                 │
│ Step 2: Load SubmissionTypes                                   │
│ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐    │
│ │SubTypeReader    │→│SubTypeProcessor │→│   JpaWriter     │    │
│ └─────────────────┘ └─────────────────┘ └─────────────────┘    │
│                                                                 │
│ Step 3: Load Groups                                            │
│ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐    │
│ │  GroupReader    │→│ GroupProcessor  │→│   JpaWriter     │    │
│ │                 │ │ (Links to Users)│ │                 │    │
│ └─────────────────┘ └─────────────────┘ └─────────────────┘    │
│                                                                 │
│ Step 4: Load VisibilityProfiles                               │
│ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐    │
│ │    VPReader     │→│   VPProcessor   │→│   JpaWriter     │    │
│ │                 │ │ (Links to All)  │ │                 │    │
│ └─────────────────┘ └─────────────────┘ └─────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### Phase 3: Detailed Step Processing (Example: User Processing)

**Step 3.1: Reading**
- TokenService fetches/refreshes authentication token
- RedOakStreamReader opens HTTP connection to `https://api.redoak.example.com/api/v1/users`
- JSON stream parser reads user objects one by one

**Step 3.2: Processing**
- UserProcessor receives UserJson record
- Transforms nested JSON structure to flat User entity
- Resolves VisibilityProfile reference via database lookup
- Converts List<String> roles to Set<String> for entity

**Step 3.3: Writing**
- JpaItemWriter batches 1000 users per transaction
- Uses `merge()` instead of `persist()` to handle updates
- Commits transaction or rolls back on error

### Phase 4: Error Handling & Monitoring

**Fault Tolerance Mechanisms**:
- **Retry**: Each step retries failed items 3 times
- **Skip**: Can skip up to 10 failed items per step
- **Transaction rollback**: Failed chunks rollback without affecting others
- **Monitoring**: JobExecutionMonitor tracks progress and failures

## Hypothetical Data Flow Example

Let's trace a single user through the system:

### Input JSON from RedOak API:
```json
{
  "userName": "alice.smith",
  "firstName": "Alice",
  "lastName": "Smith",
  "emailAddress": "alice.smith@company.com",
  "active": true,
  "roles": ["ANALYST", "REVIEWER"],
  "visibilityProfile": "StandardProfile",
  "groups": [
    {"groupName": "Analytics", "active": true},
    {"groupName": "Compliance", "active": true}
  ],
  "adminRolePermissions": [
    ["REVIEWER", "APPROVE_SUBMISSION"],
    ["REVIEWER", "REJECT_SUBMISSION"]
  ]
}
```

### Processing Steps:
1. **RedOakStreamReader** fetches this JSON from the API
2. **UserProcessor** transforms it:
   - Creates User entity with mapped fields
   - Converts roles List to Set: `{"ANALYST", "REVIEWER"}`
   - Looks up VisibilityProfile "StandardProfile" from database
   - Flattens adminRolePermissions to: `{"REVIEWER:APPROVE_SUBMISSION", "REVIEWER:REJECT_SUBMISSION"}`
3. **JpaItemWriter** saves to database in tables:
   - `users`: Main user record
   - `user_roles`: Two records for ANALYST and REVIEWER
   - `user_admin_role_permissions`: Two records for the permissions

### Later in Group Processing:
- **GroupProcessor** processes "Analytics" group
- Finds existing user "alice.smith" in database
- Creates bidirectional relationship in `group_users` table

## Key Architecture Benefits

1. **Scalability**: Chunk-based processing handles millions of records
2. **Fault Tolerance**: Retry/skip mechanisms prevent total failure
3. **Memory Efficiency**: Streaming prevents OutOfMemoryError
4. **Transactional Safety**: Each chunk is atomic
5. **Monitoring**: Built-in job execution tracking
6. **Maintainability**: Clear separation of concerns across packages

## Performance Characteristics

- **Memory Usage**: O(chunk_size) not O(total_records)
- **Processing Speed**: Configurable chunk sizes optimize for throughput vs. memory
- **Database Connections**: Connection pooling prevents resource exhaustion
- **Token Management**: Caching reduces authentication overhead
- **Error Recovery**: Granular failure handling minimizes reprocessing

This architecture provides a robust, scalable solution for large-scale data migration with comprehensive error handling and monitoring capabilities.

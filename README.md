# Sentinel: High-Concurrency Distributed Ticketing Engine

Sentinel is a production-grade backend engine designed to solve the "Flash Sale" problem: handling massive spikes in traffic while maintaining 100% data consistency and low latency.

## 🚀 The Challenge
During high-demand events (like concert ticket launches), thousands of users attempt to reserve the same resource simultaneously. A standard CRUD application would suffer from **Race Conditions** (over-selling) and **Connection Pool Exhaustion**.

Sentinel solves this using a multi-layered distributed architecture.

## 🛠 Tech Stack & Architecture
- **Language:** Java 21 (Utilizing **Virtual Threads** for high-throughput I/O).
- **Framework:** Spring Boot 3.4 (LTS).
- **Distributed Locking:** **Redisson (Redis)** to ensure atomic "Check-and-Reserve" operations.
- **Database:** PostgreSQL with **JPA Optimistic Locking** as a secondary fail-safe.
- **Messaging:** **Apache Kafka** for asynchronous, non-blocking event streaming.
- **Infrastructure:** Containerized via Docker Compose.

## 🏗 Key Technical Implementations

### 1. Distributed Lock Pattern
To prevent double-booking across multiple server instances, I implemented a distributed lock using Redisson.
- **Lease Management:** Locks are automatically released after a TTL to prevent deadlocks if a node crashes.
- **Fail-Fast:** The system returns an immediate `409 Conflict` if a resource is locked, rather than keeping the user waiting.

### 2. Transaction Optimization
I refactored the service layer to move external network calls (Kafka) **outside** the database `@Transactional` block. This prevents "Connection Leakage" and ensures the database connection pool is never held hostage by a slow message broker.

### 3. Java 21 Virtual Threads
The engine is configured to use Project Loom's Virtual Threads, allowing it to handle thousands of concurrent HTTP requests with significantly lower RAM overhead than traditional thread-per-request models.

## 📊 Performance & Load Testing
I validated the system using a custom Bash-based stress-test suite.
- **Scenario:** 50 concurrent users attempting to reserve the **exact same seat** at the same millisecond.
- **Result:**
    - **Success Rate:** 1 successful reservation (`201 Created`).
    - **Conflict Handling:** 49 rejected attempts (`409 Conflict`).
    - **Data Integrity:** 0 double-bookings; Database version incremented exactly once.

## 🚦 Getting Started

### Prerequisites
- Docker & Docker Compose
- JDK 21

### Installation
1. Clone the repo.
2. Start infrastructure:
   ```bash
   docker-compose up -d
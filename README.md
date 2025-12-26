# Sentinel: High-Concurrency Ticketing Engine

Sentinel is an enterprise-grade distributed system designed to handle high-traffic "Flash Sale" scenarios (e.g., concert ticket launches) where data integrity and low latency are critical.

## 🚀 Key Features (Phase 1)
- **High Concurrency:** Optimized with **Java 21 Virtual Threads** to handle thousands of simultaneous bookings.
- **Distributed Locking:** Implemented **Redisson (Redis)** to prevent race conditions and ticket "overselling."
- **Data Integrity:** Dual-layer protection using **Optimistic Locking** in PostgreSQL.
- **Infrastructure-as-Code:** Fully containerized environment using Docker.

## 🛠 Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.4
- **Database:** PostgreSQL
- **Cache/Locking:** Redis (Redisson)
- **Messaging:** Apache Kafka (In-progress)
- **Containerization:** Docker

## 🏗 System Architecture

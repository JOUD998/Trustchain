# TrustChain

> A blockchain-inspired donation management platform built with Spring Boot that provides transparency, integrity, and traceability for charitable campaigns, milestone approvals, and payouts.

---

## 📖 Overview

TrustChain is a secure backend application developed to improve trust in donation platforms by recording every security-relevant action inside a blockchain-inspired audit log.

Instead of storing only business data, the system stores immutable audit transactions linked together using SHA-256 hashes. This enables verification that no transaction has been modified after being recorded.

The project was developed as part of the Software Engineering course at Hochschule Bielefeld (HSBI).

---

## ✨ Features

- JWT Authentication
- Role-Based Authorization
- Campaign Management
- Donation Management
- Voucher Management
- Milestone Creation
- Milestone Approval Workflow
- Validator Voting System
- Payout Management
- Blockchain-Inspired Audit Log
- Blockchain Integrity Verification
- RESTful API
- PostgreSQL Database
- Flyway Database Migration

---

## 🏗 Architecture

The application follows a layered architecture:

```
Controller
     │
Service
     │
Repository
     │
PostgreSQL Database
```

Each layer has a single responsibility, making the application easier to maintain, test, and extend.

---

## ⛓ Blockchain-Inspired Audit Log

Every security-relevant action creates a blockchain transaction.

Examples include:

- Campaign Created
- Donation Received
- Milestone Vote Cast
- Milestone Approved
- Payout Executed

Each transaction contains:

- Current Hash
- Previous Hash
- Entity ID
- Entity Type
- Transaction Type
- Timestamp
- Created By

This creates a tamper-evident audit trail where every transaction is cryptographically linked to the previous one.

---

## 🔒 Blockchain Integrity Verification

The system verifies blockchain integrity by checking:

- Transaction hash validity
- Genesis transaction
- Previous hash consistency
- Entire blockchain chain

If any transaction has been modified, the verification process immediately detects the manipulation.

---

## 🛠 Technology Stack

### Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Maven

### Database

- PostgreSQL
- Flyway

### Security

- JWT Authentication
- BCrypt Password Encoder

### Utilities

- Lombok
- Jakarta Validation

---

## 🗄 Database Model

Main entities:

- Users
- Campaign
- Donation
- Voucher
- Milestone
- Milestone Vote
- Payout
- Blockchain Transaction

---

## 📡 REST API

Example endpoints:

```
POST /api/auth/login
POST /api/auth/register

GET /api/campaigns
POST /api/campaigns

POST /api/donations

POST /api/milestone-votes

POST /api/payouts

GET /api/blockchain/verify
```

---

## 📁 Project Structure

```
src
└── main
    ├── java
    │   └── com.joud.trustchain
    │       ├── controller
    │       ├── service
    │       ├── repository
    │       ├── entity
    │       ├── dto
    │       ├── security
    │       ├── config
    │       ├── exception
    │       └── blockchain
    └── resources
        ├── db
        │   └── migration
        └── application.properties
```

---

## 🚀 Installation

### Clone the repository

```bash
git clone https://github.com/JOUD998/Trustchain.git
```

### Navigate to the project

```bash
cd Trustchain
```

### Configure PostgreSQL

Update the database connection inside:

```
src/main/resources/application.properties
```

### Run Flyway migrations

Flyway will automatically execute all migration scripts when the application starts.

### Start the application

```bash
mvn spring-boot:run
```

---

## 🔐 Security Features

- JWT Authentication
- Role-Based Authorization
- Password Encryption using BCrypt
- Blockchain-Inspired Audit Logging
- Blockchain Integrity Verification

---

## 📊 Diagrams

The project documentation includes:

- Layered Architecture Diagram
- Entity Relationship Diagram (ERD)
- Sequence Diagram
- Blockchain Integrity Verification Activity Diagram
- Blockchain-Inspired Audit Log Diagram

---

## 🔮 Future Improvements

- Docker Support
- Microservices Architecture
- Notification Service
- Analytics Dashboard
- Smart Contract Integration
- Hyperledger Fabric Integration



---

## 📄 License

This project was developed for educational purposes as part of the Software Engineering course at Hochschule Bielefeld (HSBI).

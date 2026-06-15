# MONEY TRANSFER SYSTEM  


1. Executive Summary
    

The Money Transfer System is a production-grade enterprise banking microservice developed to process digital fund transfers securely. The system has been built progressively following a structured training-aligned curriculum. It integrates version control, advanced backend development, and a modern frontend interface to deliver a complete end-to-end banking solution.

This project focuses on ensuring secure, reliable, and efficient fund transfers while maintaining data integrity and providing a scalable architecture suitable for enterprise-level applications.

---

2. Technology Stack
    

The technologies used in the development of the Money Transfer System are listed below:

Programming Language: Java (Version 17 LTS)  
Backend Framework: Spring Boot (Version 3.4.2)  
Security Framework: Spring Security with JWT
Frontend Framework: Angular
Database: MySQL  
Data Warehouse: Snowflake

---

3. Architecture Overview
    

The system follows a multi-tier architecture designed to support secure enterprise banking operations.

- Presentation Layer:  
The frontend is developed using Angular as a Single Page Application (SPA), providing an interactive and responsive user interface.

- API Layer:  
The backend consists of RESTful services developed using Spring Boot, which handle client requests and manage business workflows.

- Business Domain Layer:  
This layer contains Java 17 domain entities and business logic responsible for validating transactions and applying business rules.

- Data Layer:  
MySQL is used as the primary relational database to store transactional data such as account information and transaction logs.

- Analytics Layer:  
Snowflake Data Warehouse is used for analytical processing and business intelligence reporting.

---

4. Key Features and Implementation
    

- Fund Transfer:  
The system supports secure fund transfers between user accounts with full ACID compliance using transactional management through the @Transactional annotation.

- JWT Authentication:  
User authentication and authorization are implemented using JSON Web Tokens (JWT) to ensure secure access to protected API endpoints.

- Idempotency:  
Duplicate transactions are prevented using a unique idempotency key for every transfer request, ensuring reliability and consistency.

- Optimistic Locking:  
Concurrency control is handled using a version field in the Account entity to avoid data inconsistencies during simultaneous updates.

- Audit Logging:  
Aspect-Oriented Programming (AOP) is used to implement audit logging, enabling tracking of method execution and system activities for monitoring and debugging purposes.

---

5. Project Structure
    

The project repository is organized into the following directory structure:

money-transfer-system  
- backend – Spring Boot REST API  
- frontend – Angular Application  
- database – MySQL schema and seed scripts  
- snowflake – Snowflake analytics scripts  
- README.md – Project overview

---

6. Getting Started
    

6.1 Prerequisites

To run the project, the following software is required:

Java 17 JDK  
Node.js and Angular CLI  
MySQL Server 8.x  
Snowflake Account

6.2 Database Setup

Execute the database schema script to create the required tables such as ACCOUNTS and TRANSACTION_LOGS.  
Run the seed data script to populate initial testing data into the database.

6.3 Backend Setup

Navigate to the backend directory.  
Update the application configuration file (application.yml) with database credentials and JWT secret key.  
Build and run the backend application using the following command:

mvn spring-boot:run

6.4 Frontend Setup

Navigate to the frontend directory.  
Install project dependencies and start the development server using the commands:

npm install  
ng serve

Open the application in a browser at:  
[http://localhost:4200](http://localhost:4200)

---

7. API Specification
    

All API endpoints are secured and require valid JWT authentication.

- POST /api/v1/transfers  
Purpose: Executes a new fund transfer between accounts.

- GET /api/v1/accounts/{id}  
Purpose: Retrieves account details.

- GET /api/v1/accounts/{id}/balance  
Purpose: Retrieves the current account balance.

- GET /api/v1/accounts/{id}/transactions  
Purpose: Retrieves the transaction history of the account.

---

8. Business Rules
    

The following business rules are enforced in the system:

- Accounts involved in a transfer must be different.

- Both source and destination accounts must be in ACTIVE status.

- Transfer amount must be greater than zero.

- Source account must have a balance greater than or equal to the transfer amount.

- Every transaction must be recorded in the system for auditing and compliance purposes.

---

Qorpy NRS Admin Portal
National Revenue Service Compliance & Monitoring System
Qorpy is a mission-critical administrative platform designed for the National Revenue Service. It provides real-time oversight of taxpayer compliance, automated invoice flagging, and a robust auditing engine to ensure financial transparency across the national grid.

Quick Tech Stack
Backend: Java 25 + Spring Boot 4.0.5

Database: PostgreSQL 16+ (via Docker)

Security: Stateless JWT with strict Role-Based Access Control (RBAC)

Migration: Flyway (Version-controlled schema)

Documentation: SpringDoc / Swagger OpenAPI 3.0

Tunneling: Cloudflare Tunnels (TryCloudflare)

System Architecture & Schema
Based on our current ERD, the system is divided into four core modules:

1. Identity & Access (IAM)
admin_users: Manages internal staff (Super Admin, Compliance Officer, Viewer). Includes security features like account locking and failed attempt tracking.

user_notification_settings: Personalization for critical alert delivery.

2. Taxpayer Intelligence
taxpayers: Profiles for entities like MTN and Dangote. Tracks kyc_status, subscription_tier, and account_status.

taxpayer_annotations: Allows compliance officers to leave "sticky notes" on specific taxpayer profiles for manual review.

3. Financial Oversight
invoices: The heart of the system. Tracks submission status and uses a compliance_flag (JSONB) to mark suspicious transactions.

invoice_history: A temporal table for tracking every state change of a financial document.

4. Alerting & Governance
audit_logs: Immutable record of every state-changing action performed by an admin.

alert_rules: A dynamic engine where admins define triggers (e.g., "Flag if invoice > 10M").

notifications: Targeted alerts delivered to specific roles based on rule triggers.

Getting Started
Prerequisites
Docker Desktop (for PostgreSQL)

Java 25 SDK

Maven 3.9+

Local Setup
Start the Database:

Bash
docker run --name qorpy-db -e POSTGRES_PASSWORD=123456789 -p 5433:5432 -d postgres
Run Database Migrations:

Bash
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5433/qorpy_nrs -Dflyway.user=postgres -Dflyway.password=123456789
Launch the Application:

Bash
mvn spring-boot:run
🔗 API Documentation (Swagger)
Once the application is running, the interactive API documentation is available at:

Local: http://localhost:8081/swagger-ui/index.html

Production (Tunnel): https://[your-cloudflare-url].trycloudflare.com/swagger-ui/index.html

Note: To test protected endpoints, use the POST /api/auth/login endpoint to generate a JWT, then click the "Authorize" button in Swagger and paste the token.

Security Implementation
The portal uses a "Zero Trust" approach:

Statelessness: No sessions are stored on the server.

JWT Blacklisting: Logging out immediately invalidates the token in the blacklisted_tokens table.

Password Policy: All passwords are hashed using BCrypt with a strength factor of 10.

CORS: Configured to allow secure cross-origin requests from Cloudflare proxy domains.

Remote Access (Cloudflare Tunnel)
To expose the local API for external testing without port forwarding:

Bash
cloudflared tunnel --url http://localhost:8081
Contributors
Engineering: Java Team Cohort 5

Project Stakeholders: Qucoon / Univaciti

License: Internal Use Only (Proprietary)
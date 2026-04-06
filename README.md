# Qorpy NRS Admin Portal
### *National Revenue Service Compliance & Monitoring System*

Qorpy is a mission-critical administrative platform designed for the National Revenue Service. It provides real-time oversight of taxpayer compliance, automated invoice flagging, and a robust auditing engine to ensure financial transparency across the national grid.

---

## Entity Relationship Diagram (ERD)

![WhatsApp Image 2026-04-05 at 23 19 29 (1)](https://github.com/user-attachments/assets/595054ab-2850-4c3b-bff0-5a426ce2d267)

---

## Quick Tech Stack
* **Backend:** Java 25 + Spring Boot 4.0.5
* **Database:** PostgreSQL 16+ (via Docker)
* **Security:** Stateless JWT with strict Role-Based Access Control (RBAC)
* **Migration:** Flyway (Version-controlled schema)
* **Documentation:** SpringDoc / Swagger OpenAPI 3.0
* **Tunneling:** Cloudflare Tunnels (TryCloudflare)

---

## System Architecture & Schema
Based on our current ERD, the system is divided into four core modules:

### 1. Identity & Access (IAM)
* **`admin_users`**: Manages internal staff (Super Admin, Compliance Officer, Viewer). Includes security features like account locking and failed attempt tracking.
* **`user_notification_settings`**: Personalization for critical alert delivery.

### 2. Taxpayer Intelligence
* **`taxpayers`**: Profiles for entities like MTN and Dangote. Tracks `kyc_status`, `subscription_tier`, and `account_status`.
* **`taxpayer_annotations`**: Allows compliance officers to leave "sticky notes" on specific taxpayer profiles for manual review.

### 3. Financial Oversight
* **`invoices`**: The heart of the system. Tracks submission status and uses a `compliance_flag` (JSONB) to mark suspicious transactions.
* **`invoice_history`**: A temporal table for tracking every state change of a financial document.

### 4. Alerting & Governance
* **`audit_logs`**: Immutable record of every state-changing action performed by an admin.
* **`alert_rules`**: A dynamic engine where admins define triggers (e.g., "Flag if invoice > 10M").
* **`notifications`**: Targeted alerts delivered to specific roles based on rule triggers.

---

## Getting Started

### Prerequisites
* Docker Desktop (for PostgreSQL)
* Java 25 SDK
* Maven 3.9+

### Local Setup

**1. Start the Database:**
```bash
docker run --name qorpy-db -e POSTGRES_PASSWORD=123456789 -p 5433:5432 -d postgres
```
**2. Run Database Migrations:**
```bash
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5433/qorpy_nrs -Dflyway.user=postgres -Dflyway.password=123456789
```
**3. Launch the Application:**

```Bash
mvn spring-boot:run
```
## API Documentation (Swagger)

Once the application is running, access the interactive API docs via:

**Local:**
http://localhost:8081/swagger-ui/index.html

**Production (Tunnel):**
https://[your-cloudflare-url].trycloudflare.com/swagger-ui/index.html

### Authentication (Protected Endpoints)

1. Call `POST /api/auth/login` to generate a JWT.
2. Click the **"Authorize"** button in Swagger UI.
3. Paste the token in the format:

---

## Security Implementation

The system follows a **Zero Trust Architecture**, ensuring every request is verified:

- **Stateless Authentication**  
No server-side sessions are maintained; all requests are independently authenticated using JWT.

- **JWT Blacklisting**  
Tokens are invalidated on logout and stored in the `blacklisted_tokens` table to prevent reuse.

- **Password Security**  
Passwords are hashed using **BCrypt** with a strength factor of 10.

- **CORS Configuration**  
Secure cross-origin access is enabled specifically for trusted domains (e.g., Cloudflare tunnels).

---

## Remote Access (Cloudflare Tunnel)

To expose the local API securely without port forwarding:

```bash
cloudflared tunnel --url http://localhost:8081
```

You can use this URL to access your API remotely from any device or share it for external testing.

---

## Contributors

- **Engineering:** Java Team Cohort 5  
- **Stakeholders:** Qucoon / Univaciti  

---

## License

This project is for **internal use only** and is considered **proprietary**.

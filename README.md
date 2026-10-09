# EquipSync

> A full-stack equipment management system designed to streamline equipment tracking, allocation, and management through a centralized web application.

**EquipSync** is a full-stack web application designed to simplify equipment management within an organization. It provides a centralized platform where administrators, managers, and employees can interact with equipment and operational workflows according to their assigned roles.

The application follows a **React + Spring Boot + PostgreSQL** architecture and uses **JWT-based authentication with HTTP cookies** and role-based access control to secure both frontend routes and backend endpoints.

The entire application can also be run as a **multi-container application using Docker Compose**.

---

# Features

## Authentication & Authorization

* JWT-based authentication using HTTP-only cookies with CSRF protection
* Secure password hashing with BCrypt
* Role-based access control
* Protected frontend routes
* Protected Spring Boot API endpoints
* Role-specific workflows and permissions
* Configurable JWT expiration
* Centralized security configuration

## Role-Based Workflows

EquipSync currently supports three primary roles:

| Role     | Purpose                                                      |
|----------|--------------------------------------------------------------|
| Admin    | System-level management and administration                   |
| Manager  | Equipment and employee-related operational management        |
| Employee | Access to assigned equipment and employee-specific workflows |

Both the frontend and backend enforce role-based access to prevent unauthorized users from accessing restricted functionality.

## Equipment Management

* Centralized equipment management
* Equipment tracking
* Equipment allocation workflows
* Equipment-related data management
* Role-based access to equipment operations
* Persistent storage using PostgreSQL

## Frontend

* React-based user interface built with TypeScript
* Tailwind CSS styling
* Axios for API communication, with interceptors handling CSRF headers
* TanStack Query for server-state management
* Protected and role-specific routes
* API integration with the Spring Boot backend

## Backend

* RESTful API architecture
* Spring Boot backend
* Spring Data JPA for database access
* Layered architecture
* DTO-based request/response handling
* Centralized exception handling
* Per-IP rate limiting (Bucket4j token bucket with a Caffeine cache)
* Unit tests
* Security and authorization layer

---

# Tech Stack

## Frontend

* React.js
* TypeScript
* Tailwind CSS
* TanStack Query
* Axios

## Backend

* Java
* Spring Boot
* Spring Data JPA
* Hibernate
* REST APIs

## Database

* PostgreSQL

## Authentication & Security

**EquipSync** uses **Spring Security** with JWT-based authentication, HTTP cookies, CSRF protection, and role-based authorization.

The authentication system is designed around the following security layers:

* JWT-based authentication using HTTP-only cookies with CSRF protection
* HTTP-only authentication cookies
* CSRF protection
* BCrypt password hashing
* Role-based access control
* Protected frontend routes
* Protected backend endpoints

## Authentication Flow

```text
┌──────────────────────┐
│        Client        │
└──────────┬───────────┘
           │
           │ Login credentials
           ▼
┌──────────────────────┐
│    Spring Boot API   │
│   Spring Security    │
└──────────┬───────────┘
           │
           │ Validate credentials
           ▼
┌──────────────────────┐
│      PostgreSQL      │
│    User Repository   │
└──────────┬───────────┘
           │
           │ Credentials valid
           ▼
┌──────────────────────┐
│      Generate JWT    │
└──────────┬───────────┘
           │
           │ Set-Cookie
           ▼
┌──────────────────────┐
│   HTTP-only Cookie   │
└──────────────────────┘
```

The JWT is stored in an HTTP-only cookie, preventing client-side JavaScript from directly accessing the authentication token.

## CSRF Protection

Because authentication is handled through cookies, browsers automatically attach the authentication cookie to requests.

To protect against Cross-Site Request Forgery (CSRF) attacks, EquipSync uses Spring Security's CSRF protection.

The general request flow is:

```text
┌──────────────┐
│   Frontend   │
└──────┬───────┘
       │
       │ Request
       │
       ├───────────────► CSRF Token
       │
       └───────────────► Authentication Cookie
                              │
                              ▼
                    ┌──────────────────┐
                    │  Spring Security │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌───────────────────┐
                    |Validate CSRF Token|
                    └────────┬──────────┘
                             │
                             ▼
                    ┌───────────────────┐
                    |Validate JWT Cookie|
                    └────────┬──────────┘
                             │
                             ▼
                    ┌───────────────────┐
                    | Check User Role   |
                    └────────┬──────────┘
                             │
                             ▼
                    ┌───────────────────┐
                    |   API Endpoint    |
                    └───────────────────┘
```

For state-changing requests such as POST, PUT, PATCH, and DELETE, the client must provide the appropriate CSRF token along with the authentication cookie.

This provides protection against malicious websites attempting to perform authenticated actions on behalf of a logged-in user.

## Authorization

After authentication, Spring Security determines whether the authenticated user has permission to access the requested resource.

**EquipSync** currently supports three primary roles:

| Role     | Access                                               |
|----------|------------------------------------------------------|
| Admin    | Administrative and system-level operations           |
| Manager  | Manager-specific equipment and operational workflows |
| Employee | Employee-specific equipment workflows                |

Authorization is enforced at the **backend level**, while the frontend also protects role-specific routes and interfaces.

```text
Request
   │
   ▼
Authentication
   │
   ├── Invalid → Reject
   │
   ▼
CSRF Validation
   │
   ├── Invalid → Reject
   │
   ▼
Role / Permission Check
   │
   ├── Unauthorized → Reject
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
Database
```

## Password Security

User passwords are never stored in plaintext.

EquipSync uses **BCrypt** to hash passwords before storing them in PostgreSQL.

```text
Plaintext Password
        │
        ▼
     BCrypt
        │
        ▼
    Password Hash
        │
        ▼
    PostgreSQL
```

During authentication, the submitted password is compared against the stored BCrypt hash rather than being directly compared with a plaintext password.

---

# Security Architecture

```text
                    ┌─────────────────────┐
                    │       Browser       │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │    HTTP Request     │
                    │                     │
                    │ • JWT Cookie        │
                    │ • CSRF Token        │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   Spring Security   │
                    ├─────────────────────┤
                    │ JWT Authentication  │
                    │ CSRF Validation     │
                    │ Role Authorization  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   REST Controller   │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │      Service        │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     Repository      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     PostgreSQL      │
                    └─────────────────────┘
```

---

# Project Structure

```text
EquipSync/
├── .gitignore
├── .dockerignore
├── docker-compose.yaml
├── .env.example
├── backend/
│   ├── .mvn/
│   │   └── wrapper/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── backend/
│   │   │   │           └── backend/
│   │   │   │               ├── config/
│   │   │   │               ├── controllers/
│   │   │   │               ├── dto/
│   │   │   │               ├── enums/
│   │   │   │               ├── exceptions/
│   │   │   │               ├── models/
│   │   │   │               ├── repository/
│   │   │   │               ├── requests/
│   │   │   │               ├── responses/
│   │   │   │               ├── security/
│   │   │   │               ├── services/
│   │   │   │               └── BackendApplication.java
│   │   │   └── resources/
│   │   │          ├── application.yaml
│   │   │          └── banner.txt
│   │   └── .env.example
│   ├── .gitattributes
│   ├── .gitignore
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── .dockerignore
│   ├── Dockerfile
│   └── pom.xml
└── frontend/
    ├── .env.example
    ├── .dockerignore
    ├── Dockerfile
    ├── package.json
    ├── package-lock.json
    ├── public/
    └── src/
        ├── assets/
        ├── components/
        ├── pages/
        │   ├── AdminPages/
        │   ├── common/
        │   └── EmployeePages/
        ├── hooks/
        ├── types/
        └── utils/
```

The backend follows a layered structure that separates HTTP handling, business logic, data access, security, and data-transfer objects.

---

# Getting Started

## Prerequisites

Make sure the following are installed:

* Java JDK
* Maven
* Node.js
* npm
* Docker
* Docker Compose

---

## 1. Clone the Repository

```bash
git clone https://github.com/Mitul82/EquipSync.git
cd EquipSync
```

---

## 2. Database Setup

EquipSync uses **PostgreSQL** as its primary database.

Run PostgreSQL with Docker:

```bash
docker run -d \
  -v "<your-local-path>/PgData:/var/lib/postgresql" \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=12345 \
  -p 5432:5432 \
  --name EquipSync-Postgre \
  postgres
```
verify that the container is running:

```bash
docker ps
```

---

## 3. Backend Configuration

Navigate to the backend:

```bash
cd backend
```

Create a **.env** file using the provided **.env.example**:

```bash
cp src/.env.example src/.env
```

Configure the required environment variables:

*Values below are placeholders. Copy .env.example and replace them with your own.*
```text
DB_URI=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=local-development-password

JWT_SECRET=replace-with-a-base64-encoded-random-key-of-at-least-32-bytes
JWT_EXPIRATION_MS=900000

FRONTEND_URL=http://localhost:5173
```

### Run the backend

Using the Maven wrapper:

#### Windows

```bash
mvnw.cmd spring-boot:run
```

#### Linux

```bash
./mvnw spring-boot:run
```

The Spring Boot application will start on the port set by `server.port` in `backend/src/main/resources/application.yaml`. Make sure `VITE_BACKEND_URL` in the frontend `.env` uses the same port.

### Run the tests

#### Windows

```bash
mvnw.cmd test
```

#### Linux

```bash
./mvnw test
```

---

## 4. Frontend Configuration

Open another terminal, go to the repository root, and navigate to the frontend:

```bash
cd frontend
```

Create the environment file:

```bash
cp .env.example .env
```

Configure the backend URL:

*Values below are placeholders. Copy .env.example and replace them with your own.*
```text
VITE_BACKEND_URL=http://localhost:3000
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will be available at the URL provided by Vite.

---

# Running with Docker Compose

**EquipSync** can be run as a multi-container application using Docker Compose.

The overall setup consists of:

```text
┌───────────────────────────────┐
│          Docker Host          │
│                               │
│  ┌─────────────────────────┐  │
│  │      Frontend           │  │
│  │       React             │  │
│  └────────────┬────────────┘  │
│               │               │
│               ▼               │
│  ┌─────────────────────────┐  │
│  │       Backend           │  │
│  │      Spring Boot        │  │
│  └────────────┬────────────┘  │
│               │               │
│               ▼               │
│  ┌─────────────────────────┐  │
│  │      PostgreSQL         │  │
│  └─────────────────────────┘  │
│                               │
└───────────────────────────────┘
```

From the repository root, create the environment file:

```bash
cp .env.example .env
```

Configure the environment file:

*Values below are placeholders. Copy .env.example and replace them with your own.*
```text
DB_URI=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=local-development-password

JWT_SECRET=replace-with-a-base64-encoded-random-key-of-at-least-32-bytes
JWT_EXPIRATION_MS=900000

FRONTEND_URL=http://localhost:5173

VITE_BACKEND_URL=http://localhost:3000

POSTGRES_USER=postgres
POSTGRES_PASSWORD=12345
POSTGRES_DB=postgres

PGADMIN_DEFAULT_EMAIL=admin@admin.com
PGADMIN_DEFAULT_PASSWORD=admin123
```

Start the complete application using:

```bash
docker-compose --env-file ./.env up --build
```

Stop the containers:

```bash
docker-compose down
```

---

# Known Limitations

* Approving two requests for the same asset at the exact same moment is not protected by row-level locking yet (planned: pessimistic locking on the asset row).
* No live deployment; the application is run locally or via Docker Compose.

---

# Future Improvements

Potential future improvements include:

* Equipment maintenance and service history
* Advanced equipment search and filtering
* Equipment availability dashboards
* Audit logs for equipment operations
* Notifications and alerts
* Advanced reporting and analytics
* Improved administrative dashboards
* Production-ready monitoring and logging

---

# Author

**Mitul Srivastava**

GitHub: [@Mitul82](https://github.com/Mitul82)

If you find EquipSync useful or interesting, consider giving the repository a ⭐.

**Repository:**
https://github.com/Mitul82/EquipSync

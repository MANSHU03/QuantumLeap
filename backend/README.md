# QuantumLeap Backend

Spring Boot 3.x backend for the QuantumLeap collaborative whiteboard application.

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL 15+

### 1. Database Setup
```bash
# Connect to PostgreSQL as superuser
psql -U postgres

# Create database and user
CREATE DATABASE quantumleap;
CREATE USER your_username WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE quantumleap TO your_username;

# Enable UUID extension
\c quantumleap
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

# Exit
\q
```

### 2. Configure Environment Variables
- Copy `src/main/resources/application.yml.example` to `application.yml` and fill in your real values.
- **Never commit your real `application.yml` to version control.**
- All secrets and credentials should be managed via environment variables or local config files.

### 3. Run the Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven directly
mvn spring-boot:run
```
The application will start on `http://localhost:8080`

## 🔧 Configuration
- All configuration should be done in `src/main/resources/application.yml` (not committed) or via environment variables.
- See `src/main/resources/application.yml.example` for the required structure and documentation.

## 🔐 Security & Best Practices
- **Never commit real secrets or credentials.**
- Add `src/main/resources/application.yml` to `.gitignore`.
- Use strong, unique secrets for JWT and database credentials in production.
- Use environment variables for sensitive values in production.

## 📁 Project Structure
```
src/main/java/com/quantumleap/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── repository/          # Data access layer
├── service/             # Business logic
├── ws/                  # WebSocket handlers
└── QuantumLeapApplication.java
src/main/resources/
└── application.yml.example  # Example config (do not commit real config)
```

## 📚 Documentation
- See `docs/API.md` for API details.
- See root `README.md` for project overview.

## 🛡️ License
This project is licensed under the MIT License.

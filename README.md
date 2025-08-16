# QuantumLeap - Real-Time Collaborative Whiteboard

A modern, real-time collaborative whiteboard application built with Spring Boot 3.x backend and React + TypeScript frontend. Features include real-time drawing, cursor tracking, shape creation, and multi-user collaboration.

## 🚀 Features
- Real-time Collaboration: Multiple users can draw simultaneously
- Live Cursor Tracking: See other users' cursors in real-time
- Drawing Tools: Pen, rectangle, circle, text, and eraser
- Public Whiteboards: All whiteboards are public and accessible to everyone
- WebSocket Communication: Real-time updates via WebSocket
- Event Sourcing: All drawing events are persisted and replayable
- Responsive Design: Modern UI with smooth interactions

## 🏗️ Architecture
- Backend: Spring Boot 3.x with WebSocket support
- Frontend: React 18 + TypeScript + Vite
- Database: PostgreSQL with JPA/Hibernate
- Real-time: WebSocket for live collaboration
- State Management: Zustand for frontend state
- Canvas: Konva.js for 2D drawing

## 📋 Prerequisites
- Java 21 (OpenJDK or Oracle JDK)
- Maven 3.8+
- Node.js 18+ and npm
- PostgreSQL 15+

## 🛠️ Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd QuantumLeap
```

### 2. Setup Database
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

### 3. Configure Environment Variables
- **Backend:** Copy `backend/src/main/resources/application.yml.example` to `application.yml` and fill in your real values. **Never commit your real `application.yml` to version control.**
- **Frontend:** Copy `frontend/.env.example` to `.env` and fill in your real values. **Never commit your real `.env` to version control.**

### 4. Start Backend
```bash
cd backend
./mvnw spring-boot:run
```
Backend will start on `http://localhost:8080`

### 5. Start Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend will start on `http://localhost:8081`

### 6. Access the Application
- **Frontend:** http://localhost:8081
- **Backend API:** http://localhost:8080/api/v1

## 🔐 Security & Best Practices
- **Never commit real secrets or credentials.** Use example/template config files and environment variables.
- Add `application.yml`, `.env`, and other sensitive files to `.gitignore`.
- Use strong, unique secrets for JWT and database credentials in production.

## 🔧 Configuration
- **Backend:** See `backend/src/main/resources/application.yml.example` for configuration structure.
- **Frontend:** See `frontend/.env.example` for environment variable structure.

## 📁 Project Structure
```
QuantumLeap/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/quantumleap/
│   │       ├── config/      # Configuration classes
│   │       ├── controller/  # REST controllers
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── entity/      # JPA entities
│   │       ├── repository/  # Data access layer
│   │       ├── service/     # Business logic
│   │       └── ws/          # WebSocket handlers
│   └── src/main/resources/
│       └── application.yml.example  # Example application config
├── frontend/                # React frontend
│   ├── src/
│   │   ├── api/            # API client functions
│   │   ├── pages/          # React components
│   │   ├── stores/         # Zustand state management
│   │   └── main.tsx        # Application entry point
│   ├── package.json
│   ├── vite.config.ts      # Vite configuration
│   └── .env.example        # Example environment config
└── README.md               # This file
```

## 📚 Documentation
- See `backend/README.md` and `frontend/README.md` for service-specific setup.

## 🛡️ License
This project is licensed under the MIT License.

## 💬 Support
For issues and questions, please create an issue in the repository.

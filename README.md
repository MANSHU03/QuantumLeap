# QuantumLeap - Real-Time Collaborative Whiteboard

A modern, real-time collaborative whiteboard application built with Spring Boot 3.x backend and React + TypeScript frontend. Features include real-time drawing, cursor tracking, shape creation, and multi-user collaboration.

## 🚀 Features

- **Real-time Collaboration**: Multiple users can draw simultaneously
- **Live Cursor Tracking**: See other users' cursors in real-time
- **Drawing Tools**: Pen, rectangle, circle, text, and eraser
- **Public Whiteboards**: All whiteboards are public and accessible to everyone
- **WebSocket Communication**: Real-time updates via WebSocket
- **Event Sourcing**: All drawing events are persisted and replayable
- **Responsive Design**: Modern UI with smooth interactions

## 🏗️ Architecture

- **Backend**: Spring Boot 3.x with WebSocket support
- **Frontend**: React 18 + TypeScript + Vite
- **Database**: PostgreSQL with JPA/Hibernate
- **Real-time**: WebSocket for live collaboration
- **State Management**: Zustand for frontend state
- **Canvas**: Konva.js for 2D drawing

## 📋 Prerequisites

- **Java 21** (OpenJDK or Oracle JDK)
- **Maven 3.8+**
- **Node.js 18+** and **npm**
- **PostgreSQL 15+**

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

### 3. Start Backend
```bash
cd backend
./mvnw spring-boot:run
```
Backend will start on `http://localhost:8080`

### 4. Start Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend will start on `http://localhost:8081`

### 5. Access the Application
- **Frontend**: http://localhost:8081
- **Backend API**: http://localhost:8080/api/v1

## 🔧 Configuration

### Backend Configuration
The backend uses Spring Boot's default configuration. Key settings can be modified in `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/quantumleap
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: your-jwt-secret-key
  expiration: 86400000
```

### Frontend Configuration
The frontend uses Vite's proxy configuration. API calls are automatically proxied to the backend. Configuration can be modified in `frontend/vite.config.ts`.

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
│       └── application.yml  # Application configuration
├── frontend/                # React frontend
│   ├── src/
│   │   ├── api/            # API client functions
│   │   ├── pages/          # React components
│   │   ├── stores/         # Zustand state management
│   │   └── main.tsx        # Application entry point
│   ├── package.json
│   └── vite.config.ts      # Vite configuration
└── README.md               # This file
```

## 🌐 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user

### Whiteboards
- `GET /api/v1/whiteboards` - Get all whiteboards
- `POST /api/v1/whiteboards` - Create new whiteboard
- `GET /api/v1/whiteboards/{id}` - Get whiteboard by ID
- `DELETE /api/v1/whiteboards/{id}` - Delete whiteboard (owner only)

### WebSocket
- `ws://localhost:8080/ws/whiteboard/{boardId}` - Real-time collaboration

## 🔒 Security

- JWT-based authentication
- All whiteboards are public (any user can access any whiteboard)
- Only whiteboard owners can delete their whiteboards
- WebSocket connections require valid JWT token

## 🚀 Deployment

### Backend Deployment
1. Build the JAR: `./mvnw clean package`
2. Run: `java -jar target/quantumleap-backend-1.0.0.jar`

### Frontend Deployment
1. Build: `npm run build`
2. Serve the `dist` folder with any static file server

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For issues and questions, please create an issue in the repository.

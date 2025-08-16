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

### 2. Run the Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven directly
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 🔧 Configuration

### Database Configuration
Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/quantumleap
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

### JWT Configuration
```yaml
jwt:
  secret: your-jwt-secret-key
  expiration: 86400000  # 24 hours in milliseconds
```

### WebSocket Configuration
```yaml
websocket:
  max-sessions-per-board: 100
  max-sessions-per-user: 10
  event-replay-chunk-size: 200
```

## 📁 Project Structure

```
src/main/java/com/quantumleap/
├── config/              # Configuration classes
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   └── JwtTokenProvider.java
├── controller/          # REST controllers
│   ├── AuthController.java
│   └── WhiteboardController.java
├── dto/                # Data Transfer Objects
│   ├── auth/           # Authentication DTOs
│   ├── whiteboard/     # Whiteboard DTOs
│   └── ws/             # WebSocket DTOs
├── entity/             # JPA entities
│   ├── User.java
│   ├── Whiteboard.java
│   ├── WhiteboardMember.java
│   └── Event.java
├── repository/         # Data access layer
│   ├── UserRepository.java
│   ├── WhiteboardRepository.java
│   ├── WhiteboardMemberRepository.java
│   └── EventRepository.java
├── service/           # Business logic
│   ├── AuthService.java
│   ├── WhiteboardService.java
│   ├── EventService.java
│   └── impl/          # Service implementations
├── ws/                # WebSocket handlers
│   └── WhiteboardWebSocketHandler.java
└── QuantumLeapApplication.java
```

## 🌐 API Endpoints

### Authentication
```
POST /api/v1/auth/register
POST /api/v1/auth/login
```

### Whiteboards
```
GET    /api/v1/whiteboards
POST   /api/v1/whiteboards
GET    /api/v1/whiteboards/{id}
DELETE /api/v1/whiteboards/{id}
POST   /api/v1/whiteboards/{id}/join
GET    /api/v1/whiteboards/{id}/members
```

### WebSocket
```
ws://localhost:8080/ws/whiteboard/{boardId}
```

## 🔒 Security

- JWT-based authentication
- All whiteboards are public (any user can access any whiteboard)
- Only whiteboard owners can delete their whiteboards
- WebSocket connections require valid JWT token

## 📊 Database Schema

### Users Table
- `id` (UUID, Primary Key)
- `name` (VARCHAR, Unique)
- `password_hash` (VARCHAR)
- `created_at` (TIMESTAMP)

### Whiteboards Table
- `id` (UUID, Primary Key)
- `name` (VARCHAR)
- `owner_id` (UUID, Foreign Key to Users)
- `created_at` (TIMESTAMP)

### Whiteboard_Members Table
- `id` (UUID, Primary Key)
- `whiteboard_id` (UUID, Foreign Key to Whiteboards)
- `user_id` (UUID, Foreign Key to Users)
- `owner` (BOOLEAN)
- `joined_at` (TIMESTAMP)

### Events Table
- `id` (UUID, Primary Key)
- `whiteboard_id` (UUID, Foreign Key to Whiteboards)
- `user_id` (UUID, Foreign Key to Users)
- `event_type` (VARCHAR)
- `payload` (TEXT)
- `ts` (TIMESTAMP)

## 🚀 Deployment

### Build JAR
```bash
./mvnw clean package
```

### Run JAR
```bash
java -jar target/quantumleap-backend-1.0.0.jar
```

### Environment Variables
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/quantumleap
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your-jwt-secret-key
export SERVER_PORT=8080
```

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthServiceTest

# Run with coverage
./mvnw test jacoco:report
```

## 📝 Logging

Logging is configured in `application.yml`:

```yaml
logging:
  level:
    com.quantumleap: DEBUG
    org.springframework.web.socket: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## 🔍 Monitoring

- **Health Check**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/prometheus`
- **Info**: `http://localhost:8080/actuator/info`

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Ensure PostgreSQL is running
   - Check database credentials in `application.yml`
   - Verify database and user exist

2. **Port Already in Use**
   - Change port in `application.yml`: `server.port: 8081`
   - Or kill the process using port 8080

3. **JWT Token Issues**
   - Check JWT secret in configuration
   - Ensure token expiration is reasonable

4. **WebSocket Connection Issues**
   - Verify WebSocket endpoint is accessible
   - Check CORS configuration
   - Ensure JWT token is valid

## 📄 License

This project is licensed under the MIT License.

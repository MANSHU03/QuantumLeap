# QuantumLeap Design Document

## 1. API Contract

### REST API (Session & Whiteboard Management)
- `POST   /api/v1/auth/register` — Register a new user
- `POST   /api/v1/auth/login` — Authenticate and receive JWT
- `GET    /api/v1/whiteboards` — List all whiteboards
- `POST   /api/v1/whiteboards` — Create a new whiteboard
- `GET    /api/v1/whiteboards/{id}` — Get whiteboard details
- `DELETE /api/v1/whiteboards/{id}` — Delete a whiteboard (owner only)
- `GET    /api/v1/whiteboards/{id}/members` — List members of a whiteboard

### WebSocket Message Structure
- **Connection:**
  - Endpoint: `ws://<host>/ws/whiteboard/{boardId}`
  - Auth: JWT token in connection params or headers
- **Message Envelope:**
```json
{
  "type": "EVENT_TYPE", // e.g., CURSOR_MOVE, DRAW, ERASE
  "payload": { ... },    // Event-specific data
  "timestamp": "2024-01-01T12:00:00Z",
  "userId": "...",
  "userName": "..."
}
```
- **Examples:**
  - `CURSOR_MOVE`: `{ x, y }`
  - `DRAW`: `{ shapeType, points, color, ... }`
  - `ERASE`: `{ shapeId }`

## 2. Data Schema

### Users Table
- `id` (UUID, PK)
- `name` (VARCHAR, unique)
- `email` (VARCHAR, unique)
- `password_hash` (VARCHAR)
- `created_at` (TIMESTAMP)

### Whiteboards Table
- `id` (UUID, PK)
- `name` (VARCHAR)
- `owner_id` (UUID, FK to users)
- `created_at` (TIMESTAMP)

### Whiteboard_Members Table
- `id` (UUID, PK)
- `whiteboard_id` (UUID, FK)
- `user_id` (UUID, FK)
- `owner` (BOOLEAN)
- `joined_at` (TIMESTAMP)

### Events Table
- `id` (UUID, PK)
- `whiteboard_id` (UUID, FK)
- `user_id` (UUID, FK)
- `event_type` (VARCHAR)
- `payload` (TEXT, JSON-encoded)
- `ts` (TIMESTAMP)

#### Event Payload Justification
- **Flexible:** JSON allows for extensible event types (drawing, cursor, erase, etc.)
- **Efficient:** Only event-specific data is stored, minimizing schema changes for new features
- **Replayable:** Enables event sourcing for whiteboard state reconstruction

## 3. Scalability Strategy

### WebSocket Layer Scaling
- **Problem:** WebSockets are stateful; with multiple backend instances behind a load balancer, clients may be routed to different servers, breaking session continuity ("sticky sessions" problem).
- **Solution:**
  - **Sticky Sessions:** Configure the load balancer to use session affinity (e.g., IP hash or cookie-based) so a client always connects to the same backend instance.
  - **Redis Pub/Sub Backplane:**
    - All backend instances subscribe to a Redis channel.
    - When an event is received from a client, the instance publishes it to Redis.
    - All instances (including the sender) receive the event and broadcast to their connected clients.
    - This ensures real-time events are synchronized across all instances, regardless of which server a client is connected to.
- **Benefits:**
  - Horizontal scalability (add more backend nodes)
  - Fault tolerance (no single point of failure)
  - Consistent real-time experience for all users

## 4. Choice of Libraries

### Frontend State Management: Zustand
- **Why Zustand?**
  - Minimal boilerplate, simple API
  - Excellent performance for real-time collaborative state
  - No provider wrapper needed (unlike Redux)
  - Easy to test and maintain
  - Scales well for medium-to-large apps

### Canvas Rendering: Konva.js
- **Why Konva.js?**
  - High-performance 2D canvas abstraction for React
  - Supports complex shapes, events, and layering
  - Declarative React bindings (`react-konva`)
  - Good documentation and active community
  - Enables efficient rendering and manipulation of thousands of shapes

---

**Security Note:**
- All secrets and credentials must be managed via environment variables or config files excluded from version control.
- JWT tokens are required for all API and WebSocket connections.

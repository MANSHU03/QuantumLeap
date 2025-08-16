# QuantumLeap Frontend

React 18 + TypeScript frontend for the QuantumLeap collaborative whiteboard application.

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- npm or yarn

### 1. Install Dependencies
```bash
npm install
```

### 2. Start Development Server
```bash
npm run dev
```

The application will start on `http://localhost:8081`

### 3. Build for Production
```bash
npm run build
```

## 🔧 Configuration

### Environment Variables
Create a `.env` file in the frontend directory:

```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_BASE_URL=ws://localhost:8080

# App Configuration
VITE_APP_NAME=QuantumLeap
VITE_APP_VERSION=1.0.0

# Development
VITE_DEV_MODE=true
VITE_ENABLE_LOGGING=true

# WebSocket Configuration
VITE_WS_RECONNECT_ATTEMPTS=5
VITE_WS_RECONNECT_DELAY=1000
VITE_WS_HEARTBEAT_INTERVAL=25000

# Canvas Configuration
VITE_CANVAS_WIDTH=1200
VITE_CANVAS_HEIGHT=800
VITE_CANVAS_GRID_SIZE=20
```

### Vite Configuration
The application uses Vite with proxy configuration for API calls. See `vite.config.ts`:

```typescript
export default defineConfig({
  plugins: [react()],
  server: {
    port: 8081,
    host: '0.0.0.0',
    strictPort: true,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
```

## 📁 Project Structure

```
src/
├── api/                 # API client functions
│   ├── authApi.ts      # Authentication API
│   └── boardsApi.ts    # Whiteboard API
├── pages/              # React components
│   ├── Login.tsx       # Login page
│   ├── Register.tsx    # Registration page
│   ├── Boards.tsx      # Whiteboard list
│   ├── Whiteboard.tsx  # Main whiteboard component
│   └── *.css           # Component styles
├── stores/             # Zustand state management
│   ├── authStore.ts    # Authentication state
│   ├── boardsStore.ts  # Whiteboard state
│   ├── eventsStore.ts  # Drawing events state
│   └── websocketStore.ts # WebSocket state
├── App.tsx             # Main application component
├── main.tsx            # Application entry point
└── index.css           # Global styles
```

## 🎨 Features

### Drawing Tools
- **Pen Tool**: Freehand drawing
- **Rectangle Tool**: Click and drag to create rectangles
- **Circle Tool**: Click and drag to create circles
- **Text Tool**: Add text to the canvas
- **Eraser Tool**: Erase drawn elements

### Real-time Collaboration
- **Live Cursor Tracking**: See other users' cursors in real-time
- **Real-time Drawing**: All drawing actions are synchronized
- **User Count**: Display number of active users
- **Connection Status**: Real-time connection indicator

### Whiteboard Management
- **Create Whiteboards**: Create new whiteboards with custom names
- **Public Access**: All whiteboards are public and accessible
- **Delete Whiteboards**: Owners can delete their whiteboards
- **Auto-join**: Users are automatically added to whiteboards

## 🛠️ Development

### Available Scripts

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run tests
npm run test

# Run tests in watch mode
npm run test:watch

# Run linting
npm run lint

# Fix linting issues
npm run lint:fix
```

### State Management

The application uses **Zustand** for state management:

- **authStore**: User authentication and session management
- **boardsStore**: Whiteboard list and management
- **eventsStore**: Drawing events and canvas state
- **websocketStore**: WebSocket connection and real-time communication

### API Integration

API calls are handled through dedicated API modules:

- **authApi.ts**: Authentication endpoints
- **boardsApi.ts**: Whiteboard management endpoints

All API calls automatically include JWT authentication headers.

### WebSocket Communication

Real-time features are powered by WebSocket connections:

- **Connection Management**: Automatic reconnection and error handling
- **Event Broadcasting**: Real-time drawing and cursor updates
- **State Synchronization**: Canvas state is synchronized across all users

## 🎯 Key Components

### Whiteboard Component
The main whiteboard component (`Whiteboard.tsx`) handles:
- Canvas rendering with Konva.js
- Drawing tool interactions
- Real-time collaboration
- Cursor tracking
- Event synchronization

### Authentication
Authentication is handled through:
- JWT token storage in localStorage
- Automatic token refresh
- Protected routes
- Session management

## 🔒 Security

- JWT-based authentication
- Secure WebSocket connections
- Input validation and sanitization
- CORS configuration for API calls

## 🚀 Deployment

### Build for Production
```bash
npm run build
```

The build output will be in the `dist` directory.

### Serve Production Build
```bash
# Using a simple HTTP server
npx serve dist

# Or using any static file server
# Copy dist/ contents to your web server
```

### Environment Configuration for Production
```env
VITE_API_BASE_URL=https://your-api-domain.com/api/v1
VITE_WS_BASE_URL=wss://your-api-domain.com
VITE_DEV_MODE=false
VITE_ENABLE_LOGGING=false
```

## 🧪 Testing

### Run Tests
```bash
npm run test
```

### Test Coverage
```bash
npm run test -- --coverage
```

## 📝 Code Style

The project uses:
- **ESLint** for code linting
- **Prettier** for code formatting
- **TypeScript** for type safety

### Linting
```bash
# Check for linting issues
npm run lint

# Fix auto-fixable issues
npm run lint:fix
```

## 🐛 Troubleshooting

### Common Issues

1. **Backend Connection Error**
   - Ensure backend is running on port 8080
   - Check proxy configuration in `vite.config.ts`
   - Verify API base URL in environment variables

2. **WebSocket Connection Issues**
   - Check WebSocket base URL configuration
   - Ensure JWT token is valid
   - Check browser console for connection errors

3. **Build Errors**
   - Clear node_modules and reinstall: `rm -rf node_modules && npm install`
   - Check TypeScript errors: `npm run type-check`
   - Verify all dependencies are installed

4. **Performance Issues**
   - Check for memory leaks in WebSocket connections
   - Optimize canvas rendering for large drawings
   - Monitor WebSocket message frequency

## 📄 License

This project is licensed under the MIT License.

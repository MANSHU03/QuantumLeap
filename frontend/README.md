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

### 2. Configure Environment Variables
- Copy `.env.example` to `.env` and fill in your real values.
- **Never commit your real `.env` to version control.**
- All secrets and credentials should be managed via environment variables or local config files.

### 3. Start Development Server
```bash
npm run dev
```
The application will start on `http://localhost:8081`

### 4. Build for Production
```bash
npm run build
```

## 🔧 Configuration
- All configuration should be done in `.env` (not committed) or via environment variables.
- See `.env.example` for the required structure and documentation.

## 🔐 Security & Best Practices
- **Never commit real secrets or credentials.**
- Add `.env` to `.gitignore`.
- Use strong, unique secrets for API keys and tokens in production.
- Use environment variables for sensitive values in production.

## 📁 Project Structure
```
src/
├── api/                 # API client functions
├── pages/               # React components
├── stores/              # Zustand state management
├── App.tsx              # Main application component
├── main.tsx             # Application entry point
└── index.css            # Global styles
.env.example             # Example environment config (do not commit real config)
```

## 📚 Documentation
- See `docs/API.md` for API details.
- See root `README.md` for project overview.

## 🛡️ License
This project is licensed under the MIT License.

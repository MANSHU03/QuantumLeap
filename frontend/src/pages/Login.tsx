import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import './Login.css';

const Login: React.FC = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const { login, isLoading, error, clearError, isBackendAvailable, checkBackendAvailability } = useAuthStore();
    const navigate = useNavigate();

    useEffect(() => {
        // Check backend availability when component mounts
        checkBackendAvailability();
    }, [checkBackendAvailability]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!isBackendAvailable) {
            return;
        }
        await login(email, password);
    };

    if (!isBackendAvailable) {
        return (
            <div className="login-container">
                <div className="login-card">
                    <h1>🚫 Backend Unavailable</h1>
                    <p>The backend server is not running or not accessible.</p>
                    <div className="backend-status">
                        <p><strong>To fix this:</strong></p>
                        <ol>
                            <li>Start the backend server: <code>cd backend && ./mvnw spring-boot:run</code></li>
                            <li>Ensure PostgreSQL is running</li>
                            <li>Check backend configuration</li>
                        </ol>
                    </div>
                    <button 
                        onClick={checkBackendAvailability} 
                        className="retry-button"
                    >
                        🔄 Retry Connection
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="login-container">
            <div className="login-card">
                <h1>Welcome to QuantumLeap</h1>
                <p>Sign in to your account</p>
                
                <form onSubmit={handleSubmit} className="login-form">
                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            placeholder="Enter your email"
                        />
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input
                            type="password"
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            placeholder="Enter your password"
                        />
                    </div>
                    
                    {error && (
                        <div className="error-message" onClick={clearError}>
                            {error}
                        </div>
                    )}
                    
                    <button type="submit" disabled={isLoading} className="login-button">
                        {isLoading ? 'Signing in...' : 'Sign In'}
                    </button>
                </form>
                
                <div className="login-footer">
                    <p>Don't have an account? <Link to="/register">Sign up</Link></p>
                </div>
            </div>
        </div>
    );
};

export default Login;

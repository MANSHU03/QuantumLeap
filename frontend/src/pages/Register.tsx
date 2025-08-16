import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import './Register.css';

const Register: React.FC = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const { register, isLoading, error, clearError, isBackendAvailable, checkBackendAvailability } = useAuthStore();
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
        try {
            await register(email, password, name);
            navigate('/login');
        } catch (error) {
            // Error is handled by the store
        }
    };

    if (!isBackendAvailable) {
        return (
            <div className="register-container">
                <div className="register-card">
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
        <div className="register-container">
            <div className="register-card">
                <h1>Create Account</h1>
                <p>Join QuantumLeap today</p>
                
                <form onSubmit={handleSubmit} className="register-form">
                    <div className="form-group">
                        <label htmlFor="name">Full Name</label>
                        <input
                            type="text"
                            id="name"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                            placeholder="Enter your full name"
                        />
                    </div>
                    
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
                            minLength={8}
                            placeholder="Enter your password (min 8 characters)"
                        />
                    </div>
                    
                    {error && (
                        <div className="error-message" onClick={clearError}>
                            {error}
                        </div>
                    )}
                    
                    <button type="submit" disabled={isLoading} className="register-button">
                        {isLoading ? 'Creating Account...' : 'Create Account'}
                    </button>
                </form>
                
                <div className="register-footer">
                    <p>Already have an account? <Link to="/login">Sign in</Link></p>
                </div>
            </div>
        </div>
    );
};

export default Register;

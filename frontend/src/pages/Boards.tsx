import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import { useBoardsStore } from '../stores/boardsStore';
import './Boards.css';

const Boards: React.FC = () => {
    const [newBoardName, setNewBoardName] = useState('');
    const [showCreateForm, setShowCreateForm] = useState(false);
    const { user, logout } = useAuthStore();
    const { 
        whiteboards, 
        isLoading, 
        error, 
        fetchBoards, 
        createBoard, 
        joinBoard, 
        clearError,
        isBackendAvailable,
        checkBackendAvailability
    } = useBoardsStore();
    const navigate = useNavigate();

    useEffect(() => {
        if (isBackendAvailable) {
            fetchBoards();
        }
    }, [fetchBoards, isBackendAvailable]);

    useEffect(() => {
        // Check backend availability when component mounts
        checkBackendAvailability();
    }, [checkBackendAvailability]);

    const handleCreateBoard = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!newBoardName.trim() || !isBackendAvailable) return;
        
        try {
            await createBoard(newBoardName.trim());
            setNewBoardName('');
            setShowCreateForm(false);
        } catch (error) {
            // Error is handled by the store
        }
    };

    const handleJoinBoard = async (boardId: string) => {
        if (!isBackendAvailable) return;
        
        try {
            await joinBoard(boardId);
            // Optionally refresh the boards list
            fetchBoards();
        } catch (error) {
            // Error is handled by the store
        }
    };

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    if (!isBackendAvailable) {
        return (
            <div className="boards-container">
                <header className="boards-header">
                    <div className="user-info">
                        <span>Welcome, {user?.name || 'User'}!</span>
                        <button onClick={handleLogout} className="logout-button">
                            Logout
                        </button>
                    </div>
                </header>
                <div className="boards-content">
                    <div className="backend-unavailable">
                        <h2>🚫 Backend Unavailable</h2>
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
            </div>
        );
    }

    return (
        <div className="boards-container">
            <header className="boards-header">
                <div className="user-info">
                    <span>Welcome, {user?.name || 'User'}!</span>
                    <button onClick={handleLogout} className="logout-button">
                        Logout
                    </button>
                </div>
            </header>
            
            <div className="boards-content">
                <button 
                    onClick={() => setShowCreateForm(!showCreateForm)} 
                    className="create-board-button"
                >
                    {showCreateForm ? 'Cancel' : '+ Create New Whiteboard'}
                </button>
                
                {showCreateForm && (
                    <form onSubmit={handleCreateBoard} className="create-board-form">
                        <input
                            type="text"
                            value={newBoardName}
                            onChange={(e) => setNewBoardName(e.target.value)}
                            placeholder="Enter whiteboard name"
                            required
                            minLength={1}
                            maxLength={255}
                        />
                        <button type="submit" disabled={isLoading || !newBoardName.trim()}>
                            {isLoading ? 'Creating...' : 'Create'}
                        </button>
                    </form>
                )}
                
                {error && (
                    <div className="error-message" onClick={clearError}>
                        {error} (click to dismiss)
                    </div>
                )}
                
                {isLoading ? (
                    <div className="loading">Loading whiteboards...</div>
                ) : (
                    <div className="boards-grid">
                        {whiteboards.length === 0 ? (
                            <div className="no-boards">
                                <p>No whiteboards yet. Create your first one!</p>
                            </div>
                        ) : (
                            whiteboards.map((board) => (
                                <div key={board.id} className="board-card">
                                    <h3>{board.name}</h3>
                                    <p>Created: {new Date(board.createdAt).toLocaleDateString()}</p>
                                    <p>Owner: {board.ownerName}</p>
                                    <p className="board-status">
                                        {board.isOwner ? '🟢 You own this' : '🔵 Public whiteboard'}
                                    </p>
                                    <div className="board-actions">
                                        <button 
                                            onClick={() => navigate(`/whiteboard/${board.id}`)} 
                                            className="open-board-button"
                                        >
                                            Open
                                        </button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Boards;

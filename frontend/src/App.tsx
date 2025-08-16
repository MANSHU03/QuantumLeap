import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import Login from './pages/Login';
import Register from './pages/Register';
import Boards from './pages/Boards';
import Whiteboard from './pages/Whiteboard';
import './App.css';

function App() {
  const { token } = useAuthStore();

  return (
    <div className="App">
      <Routes>
        <Route path="/login" element={!token ? <Login /> : <Navigate to="/boards" />} />
        <Route path="/register" element={!token ? <Register /> : <Navigate to="/boards" />} />
        <Route path="/boards" element={token ? <Boards /> : <Navigate to="/login" />} />
        <Route path="/whiteboard/:boardId" element={token ? <Whiteboard /> : <Navigate to="/login" />} />
        <Route path="/" element={<Navigate to={token ? "/boards" : "/login"} />} />
      </Routes>
    </div>
  );
}

export default App;

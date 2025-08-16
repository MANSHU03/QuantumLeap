import React, { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Stage, Layer, Line, Rect, Circle, Text } from 'react-konva';
import { useAuthStore } from '../stores/authStore';
import { useEventsStore, EventEnvelope } from '../stores/eventsStore';
import { useWebSocketStore } from '../stores/websocketStore';
import './Whiteboard.css';
import axios from 'axios';
import { boardsApi } from '../api/boardsApi';

interface CursorPosition {
  userId: string;
  userName: string;
  userColor: string;
  x: number;
  y: number;
  isActive: boolean;
}

const Whiteboard: React.FC = () => {
  const { boardId } = useParams<{ boardId: string }>();
  const navigate = useNavigate();
  const { token, user, isBackendAvailable, checkBackendAvailability } = useAuthStore();
  const { shapes, clearEvents } = useEventsStore();
  const { status, connect, disconnect, sendEvent, setCursorCallbacks } = useWebSocketStore();
  
  const [tool, setTool] = useState<'pen' | 'rect' | 'circle' | 'text' | 'eraser'>('pen');
  const [color, setColor] = useState('#000000');
  const [strokeWidth, setStrokeWidth] = useState(2);
  const [isDrawing, setIsDrawing] = useState(false);
  const [drawingPoints, setDrawingPoints] = useState<number[]>([]);
  const [textInput, setTextInput] = useState('');
  const [showTextInput, setShowTextInput] = useState(false);
  const [textPosition, setTextPosition] = useState({ x: 0, y: 0 });
  const [whiteboardName, setWhiteboardName] = useState<string>('');
  
  // Cursor tracking state
  const [cursors, setCursors] = useState<Map<string, CursorPosition>>(new Map());
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });
  
  // Add state for drag-to-size shape drawing
  const [drawingShape, setDrawingShape] = useState<
    | null
    | {
        type: 'rect' | 'circle';
        start: { x: number; y: number };
        current: { x: number; y: number };
      }
  >(null);
  
  const stageRef = useRef<any>(null);
  const cursorUpdateTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (!isBackendAvailable) {
      return;
    }
    if (!boardId || !token) {
      navigate('/boards');
      return;
    }

    // Connect to WebSocket
    connect(boardId, token);

    // Cleanup on unmount
    return () => {
      disconnect();
      clearEvents();
    };
  }, [boardId, token, connect, disconnect, clearEvents, navigate, isBackendAvailable]);

  // Add state for whiteboard ownership
  const [isOwner, setIsOwner] = useState(false);

  // Fetch whiteboard details including ownership
  useEffect(() => {
    if (!boardId || !token) return;
    (async () => {
      try {
        const whiteboard = await boardsApi.getBoardById(boardId);
        setWhiteboardName(whiteboard.name || '');
        setIsOwner(whiteboard.owner || false);
      } catch (err) {
        console.error('Error fetching whiteboard details:', err);
        setWhiteboardName('');
        setIsOwner(false);
      }
    })();
  }, [boardId, token]);

  // Delete whiteboard function
  const handleDeleteWhiteboard = async () => {
    if (!isOwner || !boardId || !token) return;
    
    if (window.confirm('Are you sure you want to delete this whiteboard? This action cannot be undone.')) {
      try {
        console.log('Attempting to delete whiteboard:', boardId);
        await boardsApi.deleteBoard(boardId);
        console.log('Whiteboard deleted successfully');
        navigate('/boards');
      } catch (err: any) {
        console.error('Error deleting whiteboard:', err);
        const errorMessage = err.response?.data?.message || err.message || 'Failed to delete whiteboard. Please try again.';
        alert(`Error: ${errorMessage}`);
      }
    }
  };

  // Set up cursor callbacks
  useEffect(() => {
    setCursorCallbacks(
      (cursorData) => {
        console.log('[Cursor] Received cursor update:', cursorData);
        setCursors(prev => {
          const newCursors = new Map(prev);
          if (cursorData.eventType === 'LEAVE') {
            newCursors.delete(cursorData.userId);
            console.log('[Cursor] User left:', cursorData.userId);
          } else {
            newCursors.set(cursorData.userId, {
              userId: cursorData.userId,
              userName: cursorData.userName,
              userColor: cursorData.userColor,
              x: cursorData.x,
              y: cursorData.y,
              isActive: true
            });
            console.log('[Cursor] User cursor updated:', cursorData.userId, 'at', cursorData.x, cursorData.y);
          }
          return newCursors;
        });
      },
      (cursorsData) => {
        console.log('[Cursor] Received cursors init:', cursorsData);
        const cursorsArray = Array.isArray(cursorsData) ? cursorsData : [cursorsData];
        const cursorsMap = new Map();
        cursorsArray.forEach((cursor: any) => {
          // Only filter out the local user's own cursor for themselves
          if (cursor.userId !== user?.id) {
            cursorsMap.set(cursor.userId, {
              userId: cursor.userId,
              userName: cursor.userName,
              userColor: cursor.userColor,
              x: cursor.x,
              y: cursor.y,
              isActive: true
            });
            console.log('[Cursor] Initial cursor for user:', cursor.userId, 'at', cursor.x, cursor.y);
          }
        });
        setCursors(cursorsMap);
        console.log('[Cursor] Total cursors set:', cursorsMap.size);
      }
    );
  }, [setCursorCallbacks, user?.id]);

  // Throttled cursor position update - reduce throttling for more responsive movement
  const updateCursorPosition = (x: number, y: number) => {
    setMousePosition({ x, y });
    
    // Send cursor updates immediately for better responsiveness
    if (status === 'connected' && boardId && user) {
      const cursorEvent: EventEnvelope = {
        id: `cursor-${Date.now()}`,
        boardId: boardId,
        userId: user.id,
        type: 'CURSOR_MOVE',
        ts: new Date().toISOString(),
        data: { x, y },
        tempId: `cursor-${Date.now()}`,
      };
      console.log('[Cursor] Sending cursor update:', cursorEvent);
      sendEvent(cursorEvent);
    }
  };

  const handleMouseDown = (e: any) => {
    const pos = e.target.getStage().getPointerPosition();
    if (tool === 'pen') {
      setIsDrawing(true);
      setDrawingPoints([pos.x, pos.y]);
      updateCursorPosition(pos.x, pos.y);
    } else if (tool === 'text') {
      setTextPosition(pos);
      setShowTextInput(true);
      updateCursorPosition(pos.x, pos.y);
    } else if (tool === 'eraser') {
      handleEraser(e);
      updateCursorPosition(pos.x, pos.y);
    } else if (tool === 'rect' || tool === 'circle') {
      setDrawingShape({ type: tool, start: pos, current: pos });
      updateCursorPosition(pos.x, pos.y);
    }
  };

  const handleMouseMove = (e: any) => {
    const pos = e.target.getStage().getPointerPosition();
    if (pos) {
      updateCursorPosition(pos.x, pos.y);
    }
    
    if (isDrawing && tool === 'pen') {
      setDrawingPoints([...drawingPoints, pos.x, pos.y]);
    }
    if (drawingShape) {
      setDrawingShape({ ...drawingShape, current: pos });
    }
  };

  const handleMouseUp = () => {
    if (isDrawing && tool === 'pen' && drawingPoints.length >= 4) {
      // Send line drawn event
      const event: EventEnvelope = {
        id: `temp-${Date.now()}`,
        boardId: boardId!,
        userId: user!.id,
        type: 'LINE_DRAWN',
        ts: new Date().toISOString(),
        data: {
          points: drawingPoints,
          strokeWidth,
          color,
        },
        tempId: `temp-${Date.now()}`,
      };
      
      sendEvent(event);
      setDrawingPoints([]);
    }
    setIsDrawing(false);
    // Handle drag-to-size shape finalize
    if (drawingShape) {
      const { type, start, current } = drawingShape;
      if (type === 'rect') {
        const x = Math.min(start.x, current.x);
        const y = Math.min(start.y, current.y);
        const width = Math.abs(current.x - start.x);
        const height = Math.abs(current.y - start.y);
        if (width > 5 && height > 5) {
          const event: EventEnvelope = {
            id: `temp-${Date.now()}`,
            boardId: boardId!,
            userId: user!.id,
            type: 'SHAPE_ADDED',
            ts: new Date().toISOString(),
            data: {
              id: `temp-${Date.now()}`,
              kind: 'rect',
              x,
              y,
              width,
              height,
              style: {
                fill: color,
                stroke: '#000000',
                strokeWidth: 1,
              },
            },
            tempId: `temp-${Date.now()}`,
          };
          sendEvent(event);
        }
      } else if (type === 'circle') {
        // Use start as center, radius as distance to current
        const dx = current.x - start.x;
        const dy = current.y - start.y;
        const radius = Math.sqrt(dx * dx + dy * dy);
        if (radius > 5) {
          const event: EventEnvelope = {
            id: `temp-${Date.now()}`,
            boardId: boardId!,
            userId: user!.id,
            type: 'SHAPE_ADDED',
            ts: new Date().toISOString(),
            data: {
              id: `temp-${Date.now()}`,
              kind: 'circle',
              x: start.x,
              y: start.y,
              radius,
              style: {
                fill: color,
                stroke: '#000000',
                strokeWidth: 1,
              },
            },
            tempId: `temp-${Date.now()}`,
          };
          sendEvent(event);
        }
      }
      setDrawingShape(null);
    }
  };

  const handleShapeAdd = (e: any) => {
    const pos = e.target.getStage().getPointerPosition();
    updateCursorPosition(pos.x, pos.y);
    if (tool === 'text') {
      setTextPosition(pos);
      setShowTextInput(true);
    }
  };

  const handleTextSubmit = () => {
    if (textInput.trim()) {
      const event: EventEnvelope = {
        id: `temp-${Date.now()}`,
        boardId: boardId!,
        userId: user!.id,
        type: 'TEXT_ADDED',
        ts: new Date().toISOString(),
        data: {
          id: `temp-${Date.now()}`,
          kind: 'text',
          x: textPosition.x,
          y: textPosition.y,
          text: textInput,
          style: {
            fontSize: 16,
            fill: color,
            fontFamily: 'Arial',
          },
        },
        tempId: `temp-${Date.now()}`,
      };
      sendEvent(event);
      setTextInput('');
      setShowTextInput(false);
    }
  };

  const handleEraser = (e: any) => {
    if (tool !== 'eraser') return;
    
    const pos = e.target.getStage().getPointerPosition();
    if (!pos) return;
    
    // Find all shapes under the cursor (not just the first)
    const shapesArray = Array.from(shapes.values());
    let erased = false;
    shapesArray.forEach((shape) => {
      let isUnderCursor = false;
      switch (shape.type) {
        case 'line':
          if (shape.points && shape.points.length >= 2) {
            for (let i = 0; i < shape.points.length; i += 2) {
              const dx = shape.points[i] - pos.x;
              const dy = shape.points[i + 1] - pos.y;
              if (Math.sqrt(dx * dx + dy * dy) < 24) { // Larger hit area
                isUnderCursor = true;
                break;
              }
            }
          }
          break;
        case 'rect':
          isUnderCursor = pos.x >= shape.x && pos.x <= shape.x + (shape.width || 0) &&
                        pos.y >= shape.y && pos.y <= shape.y + (shape.height || 0);
          break;
        case 'circle':
          const dx = pos.x - shape.x;
          const dy = pos.y - shape.y;
          isUnderCursor = Math.sqrt(dx * dx + dy * dy) <= (shape.radius ? shape.radius + 8 : 8); // Larger hit area
          break;
        case 'text':
          const textWidth = (shape.text?.length || 0) * 10;
          const textHeight = 20;
          isUnderCursor = pos.x >= shape.x && pos.x <= shape.x + textWidth &&
                        pos.y >= shape.y && pos.y <= shape.y + textHeight;
          break;
      }
      if (isUnderCursor) {
        // Send erase event for each shape under cursor
        const event: EventEnvelope = {
          id: shape.id, // Use the real shape id
          boardId: boardId!,
          userId: user!.id,
          type: 'SHAPE_DELETED',
          ts: new Date().toISOString(),
          data: {
            id: shape.id,
          },
        };
        sendEvent(event);
        erased = true;
      }
    });
    // Optionally, prevent default if something was erased
    if (erased) e.evt.preventDefault();
  };

  const getConnectionStatusColor = () => {
    switch (status) {
      case 'connected': return '#4CAF50';
      case 'connecting': return '#FF9800';
      case 'reconnecting': return '#FF9800';
      case 'disconnected': return '#F44336';
      default: return '#9E9E9E';
    }
  };

  if (!isBackendAvailable) {
    return (
      <div className="whiteboard-container">
        <header className="whiteboard-header">
          <div className="header-left">
            <button onClick={() => navigate('/boards')} className="back-button">
              ← Back to Boards
            </button>
            <h2>Whiteboard: {boardId}</h2>
          </div>
        </header>
        <div className="whiteboard-content">
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
    <div className="whiteboard-container">
      <header className="whiteboard-header">
        <div className="header-left">
          <button onClick={() => navigate('/boards')} className="back-button">
            ← Back to Boards
          </button>
          <h2>
            Whiteboard: {whiteboardName || boardId}
          </h2>
          {cursors.size >= 0 && (
            <div className="user-count">
              👥 {cursors.size } users online
            </div>
          )}

          {isOwner && (
            <button 
              onClick={handleDeleteWhiteboard} 
              className="delete-board-button"
              title="Delete this whiteboard (owner only)"
            >
              🗑️ Delete
            </button>
          )}
        </div>
        
        <div className="connection-status">
          <div 
            className="status-indicator" 
            style={{ backgroundColor: getConnectionStatusColor() }}
          />
          <span className="status-text">
            {status === 'connected' && 'Connected'}
            {status === 'connecting' && 'Connecting...'}
            {status === 'reconnecting' && 'Reconnecting...'}
            {status === 'disconnected' && 'Disconnected'}
          </span>
        </div>
      </header>

      <div className="whiteboard-content">
        <div className="toolbar">
          <div className="tool-group">
            <button
              className={`tool-button ${tool === 'pen' ? 'active' : ''}`}
              onClick={() => setTool('pen')}
            >
              ✏️ Pen
            </button>
            <button
              className={`tool-button ${tool === 'rect' ? 'active' : ''}`}
              onClick={() => setTool('rect')}
            >
              ⬜ Rectangle
            </button>
            <button
              className={`tool-button ${tool === 'circle' ? 'active' : ''}`}
              onClick={() => setTool('circle')}
            >
              ⭕ Circle
            </button>
            <button
              className={`tool-button ${tool === 'text' ? 'active' : ''}`}
              onClick={() => setTool('text')}
            >
              T Text
            </button>
            <button
              className={`tool-button ${tool === 'eraser' ? 'active' : ''}`}
              onClick={() => setTool('eraser')}
            >
              🧽 Eraser
            </button>

            <button
              className="tool-button danger"
              onClick={() => {
                if (status === 'connected') {
                  const event: EventEnvelope = {
                    id: `clear-${Date.now()}`,
                    boardId: boardId!,
                    userId: user!.id,
                    type: 'CLEAR_CANVAS',
                    ts: new Date().toISOString(),
                    data: {},
                    tempId: `clear-${Date.now()}`,
                  };
                  sendEvent(event);
                }
              }}
            >
              🗑️ Clear Canvas
            </button>
          </div>

          <div className="tool-group">
            <label>Color:</label>
            <input
              type="color"
              value={color}
              onChange={(e) => setColor(e.target.value)}
              className="color-picker"
            />
          </div>

          <div className="tool-group">
            <label>Width:</label>
            <input
              type="range"
              min="1"
              max="20"
              value={strokeWidth}
              onChange={(e) => setStrokeWidth(Number(e.target.value))}
              className="width-slider"
            />
            <span>{strokeWidth}</span>
          </div>
        </div>

        <div className="canvas-container">
          <Stage
            ref={stageRef}
            width={1200}
            height={800}
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onClick={handleShapeAdd}
            className="konva-stage"
            style={{ cursor: tool === 'eraser' ? 'url("data:image/svg+xml;utf8,<svg xmlns=\'http://www.w3.org/2000/svg\' width=\'32\' height=\'32\' viewBox=\'0 0 32 32\'><rect x=\'6\' y=\'20\' width=\'20\' height=\'8\' rx=\'2\' fill=\'%23e0e0e0\' stroke=\'%23999\' stroke-width=\'2\'/><rect x=\'8\' y=\'8\' width=\'16\' height=\'16\' rx=\'4\' fill=\'%23fff\' stroke=\'%23999\' stroke-width=\'2\'/></svg>\') 8 24, pointer' : 'default' }}
          >
            <Layer>
              {/* Render existing shapes */}
              {Array.from(shapes.values()).map((shape) => {
                switch (shape.type) {
                  case 'line':
                    return (
                      <Line
                        key={shape.id}
                        points={shape.points}
                        stroke={shape.style.stroke}
                        strokeWidth={shape.style.strokeWidth}
                        tension={0.5}
                        lineCap="round"
                        lineJoin="round"
                      />
                    );
                  case 'rect':
                    return (
                      <Rect
                        key={shape.id}
                        x={shape.x}
                        y={shape.y}
                        width={shape.width}
                        height={shape.height}
                        fill={shape.style.fill}
                        stroke={shape.style.stroke}
                        strokeWidth={shape.style.strokeWidth}
                      />
                    );
                  case 'circle':
                    return (
                      <Circle
                        key={shape.id}
                        x={shape.x}
                        y={shape.y}
                        radius={shape.radius}
                        fill={shape.style.fill}
                        stroke={shape.style.stroke}
                        strokeWidth={shape.style.strokeWidth}
                      />
                    );
                  case 'text':
                    return (
                      <Text
                        key={shape.id}
                        x={shape.x}
                        y={shape.y}
                        text={shape.text}
                        fontSize={shape.style.fontSize}
                        fill={shape.style.fill}
                        fontFamily={shape.style.fontFamily}
                      />
                    );
                  default:
                    return null;
                }
              })}

              {/* Render current drawing line */}
              {isDrawing && drawingPoints.length >= 2 && (
                <Line
                  points={drawingPoints}
                  stroke={color}
                  strokeWidth={strokeWidth}
                  tension={0.5}
                  lineCap="round"
                  lineJoin="round"
                />
              )}

              {/* Render user cursors */}
              {Array.from(cursors.values()).map((cursor) => (
                <React.Fragment key={cursor.userId}>
                  {/* Cursor pointer */}
                  <Circle
                    x={cursor.x}
                    y={cursor.y}
                    radius={8}
                    fill={cursor.userColor}
                    stroke="#ffffff"
                    strokeWidth={2}
                    shadowBlur={10}
                    shadowColor={cursor.userColor}
                  />
                  {/* Username label */}
                  <Text
                    x={cursor.x + 15}
                    y={cursor.y - 10}
                    text={cursor.userName}
                    fontSize={12}
                    fill={cursor.userColor}
                    fontFamily="Arial"
                    fontStyle="bold"
                    shadowBlur={5}
                    shadowColor="#000000"
                  />
                </React.Fragment>
              ))}

              {/* Render drag-to-size shape preview */}
              {drawingShape && (
                drawingShape.type === 'rect' ? (
                  <Rect
                    x={Math.min(drawingShape.start.x, drawingShape.current.x)}
                    y={Math.min(drawingShape.start.y, drawingShape.current.y)}
                    width={Math.abs(drawingShape.current.x - drawingShape.start.x)}
                    height={Math.abs(drawingShape.current.y - drawingShape.start.y)}
                    fill={color}
                    opacity={0.4}
                    stroke="#000"
                    strokeWidth={1}
                    dash={[6, 4]}
                  />
                ) : (
                  <Circle
                    x={drawingShape.start.x}
                    y={drawingShape.start.y}
                    radius={Math.sqrt(
                      Math.pow(drawingShape.current.x - drawingShape.start.x, 2) +
                      Math.pow(drawingShape.current.y - drawingShape.start.y, 2)
                    )}
                    fill={color}
                    opacity={0.4}
                    stroke="#000"
                    strokeWidth={1}
                    dash={[6, 4]}
                  />
                )
              )}
            </Layer>
          </Stage>
        </div>
      </div>

      {/* Text input modal */}
      {showTextInput && (
        <div className="text-input-modal">
          <div className="text-input-content">
            <input
              type="text"
              value={textInput}
              onChange={(e) => setTextInput(e.target.value)}
              placeholder="Enter text..."
              autoFocus
              className="text-input"
            />
            <div className="text-input-actions">
              <button onClick={handleTextSubmit} className="submit-button">
                Add
              </button>
              <button onClick={() => setShowTextInput(false)} className="cancel-button">
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Whiteboard;

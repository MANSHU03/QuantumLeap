import { create } from 'zustand';

export interface EventEnvelope {
  id: string;
  boardId: string;
  userId: string;
  type: string;
  ts: string;
  data: any;
  tempId?: string;
}

export interface CanvasShape {
  id: string;
  type: 'line' | 'rect' | 'circle' | 'text';
  x: number;
  y: number;
  width?: number;
  height?: number;
  radius?: number;
  text?: string;
  points?: number[];
  style: {
    fill?: string;
    stroke?: string;
    strokeWidth?: number;
    fontSize?: number;
    fontFamily?: string;
  };
}

interface EventsState {
  events: Map<string, EventEnvelope>;
  shapes: Map<string, CanvasShape>;
  isLoading: boolean;
  error: string | null;
}

interface EventsActions {
  addEvent: (event: EventEnvelope) => void;
  removeEvent: (eventId: string) => void;
  clearEvents: () => void;
  applyEvent: (event: EventEnvelope) => void;
  getShapes: () => CanvasShape[];
  clearError: () => void;
}

export const useEventsStore = create<EventsState & EventsActions>((set, get) => ({
  events: new Map(),
  shapes: new Map(),
  isLoading: false,
  error: null,

  addEvent: (event: EventEnvelope) => {
    set((state) => {
      const newEvents = new Map(state.events);
      newEvents.set(event.id, event);
      return { events: newEvents };
    });
    get().applyEvent(event);
  },

  removeEvent: (eventId: string) => {
    set((state) => {
      const newEvents = new Map(state.events);
      newEvents.delete(eventId);
      return { events: newEvents };
    });
  },

  clearEvents: () => {
    set({ events: new Map(), shapes: new Map() });
  },

  applyEvent: (event: EventEnvelope) => {
    set((state) => {
      const newShapes = new Map(state.shapes);
      
      switch (event.type) {
        case 'LINE_DRAWN':
          const lineShape: CanvasShape = {
            id: event.id,
            type: 'line',
            x: 0,
            y: 0,
            points: event.data.points,
            style: {
              stroke: event.data.color,
              strokeWidth: event.data.strokeWidth,
            },
          };
          newShapes.set(event.id, lineShape);
          break;

        case 'SHAPE_ADDED':
          const shape: CanvasShape = {
            id: event.id,
            type: event.data.kind,
            x: event.data.x,
            y: event.data.y,
            width: event.data.width,
            height: event.data.height,
            radius: event.data.radius,
            text: event.data.text,
            style: event.data.style,
          };
          newShapes.set(event.id, shape);
          break;

        case 'SHAPE_MOVED':
          const existingShape = newShapes.get(event.id);
          if (existingShape) {
            existingShape.x = event.data.x;
            existingShape.y = event.data.y;
            newShapes.set(event.id, existingShape);
          }
          break;

        case 'SHAPE_UPDATED':
          const shapeToUpdate = newShapes.get(event.id);
          if (shapeToUpdate) {
            Object.assign(shapeToUpdate.style, event.data.style);
            newShapes.set(event.id, shapeToUpdate);
          }
          break;

        case 'SHAPE_DELETED':
          if (event.data && event.data.id) {
            newShapes.delete(event.data.id);
          }
          break;

        case 'CLEAR_CANVAS':
          return { shapes: new Map(), events: new Map() };

        case 'TEXT_ADDED':
          const textShape: CanvasShape = {
            id: event.id,
            type: 'text',
            x: event.data.x,
            y: event.data.y,
            text: event.data.text,
            style: event.data.style,
          };
          newShapes.set(event.id, textShape);
          break;
      }

      return { shapes: newShapes };
    });
  },

  getShapes: () => {
    return Array.from(get().shapes.values());
  },

  clearError: () => {
    set({ error: null });
  },
}));

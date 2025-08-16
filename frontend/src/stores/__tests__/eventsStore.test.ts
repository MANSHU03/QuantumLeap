import { renderHook, act } from '@testing-library/react';
import { useEventsStore } from '../eventsStore';

describe('EventsStore', () => {
  beforeEach(() => {
    const { result } = renderHook(() => useEventsStore());
    act(() => {
      result.current.clearEvents();
    });
  });

  it('should add and apply events correctly', () => {
    const { result } = renderHook(() => useEventsStore());

    const event = {
      id: 'test-event-1',
      boardId: 'board-1',
      userId: 'user-1',
      type: 'SHAPE_ADDED',
      ts: new Date().toISOString(),
      data: {
        id: 'shape-1',
        kind: 'rect',
        x: 100,
        y: 100,
        width: 50,
        height: 50,
        style: { fill: '#ff0000' }
      }
    };

    act(() => {
      result.current.addEvent(event);
    });

    expect(result.current.events.size).toBe(1);
    expect(result.current.shapes.size).toBe(1);
    
    const shapes = result.current.getShapes();
    expect(shapes).toHaveLength(1);
    expect(shapes[0].id).toBe('shape-1');
    expect(shapes[0].type).toBe('rect');
  });

  it('should handle LINE_DRAWN events correctly', () => {
    const { result } = renderHook(() => useEventsStore());

    const event = {
      id: 'line-event-1',
      boardId: 'board-1',
      userId: 'user-1',
      type: 'LINE_DRAWN',
      ts: new Date().toISOString(),
      data: {
        points: [100, 100, 200, 200],
        strokeWidth: 2,
        color: '#000000'
      }
    };

    act(() => {
      result.current.addEvent(event);
    });

    const shapes = result.current.getShapes();
    expect(shapes).toHaveLength(1);
    expect(shapes[0].type).toBe('line');
    expect(shapes[0].points).toEqual([100, 100, 200, 200]);
  });

  it('should clear events correctly', () => {
    const { result } = renderHook(() => useEventsStore());

    const event = {
      id: 'test-event-1',
      boardId: 'board-1',
      userId: 'user-1',
      type: 'SHAPE_ADDED',
      ts: new Date().toISOString(),
      data: { id: 'shape-1', kind: 'rect', x: 0, y: 0, style: {} }
    };

    act(() => {
      result.current.addEvent(event);
    });

    expect(result.current.events.size).toBe(1);

    act(() => {
      result.current.clearEvents();
    });

    expect(result.current.events.size).toBe(0);
    expect(result.current.shapes.size).toBe(0);
  });
});

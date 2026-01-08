import { renderHook, act } from '@testing-library/react';
import { ComparisonProvider, useComparison } from '../ComparisonContext';
import { Technology } from '../../types/api';

// Mock the services
jest.mock('../../services', () => ({
  comparisonService: {
    detailedCompare: jest.fn(),
    saveComparison: jest.fn(),
    getComparisonById: jest.fn(),
  },
  inventoryService: {
    searchTechnologies: jest.fn(),
  },
}));

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

const mockTechnology: Technology = {
  id: 1,
  name: 'React',
  category: 'Frontend Framework',
  description: 'A JavaScript library for building user interfaces',
  metrics: { popularity: 95, performance: 85 },
  tags: ['javascript', 'frontend'],
  createdAt: '2024-01-09T10:00:00Z',
};

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <ComparisonProvider>{children}</ComparisonProvider>
);

describe('ComparisonContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorageMock.getItem.mockReturnValue(null);
  });

  it('should initialize with empty state', () => {
    const { result } = renderHook(() => useComparison(), { wrapper });

    expect(result.current.state.selectedTechnologies).toEqual([]);
    expect(result.current.state.userConstraints.priorityTags).toEqual([]);
    expect(result.current.state.comparisonResult).toBeNull();
    expect(result.current.state.isLoading).toBe(false);
    expect(result.current.state.error).toBeNull();
  });

  it('should add technology to selection', () => {
    const { result } = renderHook(() => useComparison(), { wrapper });

    act(() => {
      result.current.addTechnology(mockTechnology);
    });

    expect(result.current.state.selectedTechnologies).toHaveLength(1);
    expect(result.current.state.selectedTechnologies[0]).toEqual(mockTechnology);
  });

  it('should prevent duplicate technologies', () => {
    const { result } = renderHook(() => useComparison(), { wrapper });

    act(() => {
      result.current.addTechnology(mockTechnology);
      result.current.addTechnology(mockTechnology); // Try to add same technology
    });

    expect(result.current.state.selectedTechnologies).toHaveLength(1);
  });

  it('should remove technology from selection', () => {
    const { result } = renderHook(() => useComparison(), { wrapper });

    act(() => {
      result.current.addTechnology(mockTechnology);
    });

    expect(result.current.state.selectedTechnologies).toHaveLength(1);

    act(() => {
      result.current.removeTechnology(mockTechnology.id);
    });

    expect(result.current.state.selectedTechnologies).toHaveLength(0);
  });

  it('should update user constraints', () => {
    const { result } = renderHook(() => useComparison(), { wrapper });

    act(() => {
      result.current.updateConstraints({
        priorityTags: ['performance', 'learning-curve'],
        projectType: 'web-app',
      });
    });

    expect(result.current.state.userConstraints.priorityTags).toEqual(['performance', 'learning-curve']);
    expect(result.current.state.userConstraints.projectType).toBe('web-app');
  });

  it('should add priority tag', () => {
    const { result } = renderHook(() => useComparison(), { wrapper });

    act(() => {
      result.current.addPriorityTag('performance');
    });

    expect(result.current.state.userConstraints.priorityTags).toContain('performance');
  });

  it('should remove priority tag', () => {
    const { result } = renderHook(() => useComparison(), { wrapper });

    act(() => {
      result.current.updateConstraints({
        priorityTags: ['performance', 'learning-curve']
      });
    });

    expect(result.current.state.userConstraints.priorityTags).toHaveLength(2);

    act(() => {
      result.current.removePriorityTag('performance');
    });

    expect(result.current.state.userConstraints.priorityTags).toHaveLength(1);
    expect(result.current.state.userConstraints.priorityTags).not.toContain('performance');
  });

  it('should check if comparison is possible', () => {
    const { result } = renderHook(() => useComparison(), { wrapper });

    // Initially should not be able to compare (less than 2 technologies)
    expect(result.current.canCompare()).toBe(false);

    act(() => {
      result.current.addTechnology(mockTechnology);
    });

    // Still can't compare with only 1 technology
    expect(result.current.canCompare()).toBe(false);

    act(() => {
      result.current.addTechnology({
        ...mockTechnology,
        id: 2,
        name: 'Vue.js',
      });
    });

    // Now should be able to compare with 2 technologies
    expect(result.current.canCompare()).toBe(true);
  });

  it('should clear all technologies', () => {
    const { result } = renderHook(() => useComparison(), { wrapper });

    act(() => {
      result.current.addTechnology(mockTechnology);
      result.current.addTechnology({
        ...mockTechnology,
        id: 2,
        name: 'Vue.js',
      });
    });

    expect(result.current.state.selectedTechnologies).toHaveLength(2);

    act(() => {
      result.current.clearTechnologies();
    });

    expect(result.current.state.selectedTechnologies).toHaveLength(0);
  });

  it('should enforce maximum technology limit', () => {
    const { result } = renderHook(() => useComparison(), { wrapper });

    // Add 5 technologies (the maximum)
    for (let i = 1; i <= 5; i++) {
      act(() => {
        result.current.addTechnology({
          ...mockTechnology,
          id: i,
          name: `Technology ${i}`,
        });
      });
    }

    expect(result.current.state.selectedTechnologies).toHaveLength(5);

    // Try to add a 6th technology
    act(() => {
      result.current.addTechnology({
        ...mockTechnology,
        id: 6,
        name: 'Technology 6',
      });
    });

    // Should still have only 5 technologies and show an error
    expect(result.current.state.selectedTechnologies).toHaveLength(5);
    expect(result.current.state.error).toBeTruthy();
    expect(result.current.state.error?.message).toContain('Maximum 5 technologies');
  });
});
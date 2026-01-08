import React, { createContext, useContext, useReducer, useCallback, useEffect } from 'react';
import {
  Technology,
  UserConstraints,
  ComparisonResult,
  ApiError,
} from '../types/api';
import { comparisonService, inventoryService } from '../services';

// State interface
interface ComparisonState {
  // Selected technologies for comparison
  selectedTechnologies: Technology[];
  
  // User constraints and preferences
  userConstraints: UserConstraints;
  
  // Comparison result
  comparisonResult: ComparisonResult | null;
  
  // Loading states
  isLoading: boolean;
  isLoadingTechnologies: boolean;
  isGeneratingComparison: boolean;
  
  // Error states
  error: ApiError | null;
  
  // Session management
  sessionId: string | null;
  
  // UI state
  showAdvancedOptions: boolean;
  maxTechnologies: number;
}

// Action types
type ComparisonAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_LOADING_TECHNOLOGIES'; payload: boolean }
  | { type: 'SET_GENERATING_COMPARISON'; payload: boolean }
  | { type: 'SET_ERROR'; payload: ApiError | null }
  | { type: 'ADD_TECHNOLOGY'; payload: Technology }
  | { type: 'REMOVE_TECHNOLOGY'; payload: number }
  | { type: 'CLEAR_TECHNOLOGIES' }
  | { type: 'SET_TECHNOLOGIES'; payload: Technology[] }
  | { type: 'UPDATE_CONSTRAINTS'; payload: Partial<UserConstraints> }
  | { type: 'SET_COMPARISON_RESULT'; payload: ComparisonResult | null }
  | { type: 'SET_SESSION_ID'; payload: string | null }
  | { type: 'TOGGLE_ADVANCED_OPTIONS' }
  | { type: 'RESET_STATE' };

// Initial state
const initialState: ComparisonState = {
  selectedTechnologies: [],
  userConstraints: {
    priorityTags: [],
    projectType: undefined,
    teamSize: undefined,
    timeline: undefined,
  },
  comparisonResult: null,
  isLoading: false,
  isLoadingTechnologies: false,
  isGeneratingComparison: false,
  error: null,
  sessionId: null,
  showAdvancedOptions: false,
  maxTechnologies: 5,
};

// Reducer function
function comparisonReducer(state: ComparisonState, action: ComparisonAction): ComparisonState {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, isLoading: action.payload };
    
    case 'SET_LOADING_TECHNOLOGIES':
      return { ...state, isLoadingTechnologies: action.payload };
    
    case 'SET_GENERATING_COMPARISON':
      return { ...state, isGeneratingComparison: action.payload };
    
    case 'SET_ERROR':
      return { ...state, error: action.payload };
    
    case 'ADD_TECHNOLOGY':
      // Prevent duplicates and enforce max limit
      if (state.selectedTechnologies.some(tech => tech.id === action.payload.id)) {
        return state;
      }
      if (state.selectedTechnologies.length >= state.maxTechnologies) {
        return {
          ...state,
          error: {
            message: `Maximum ${state.maxTechnologies} technologies can be compared`,
            status: 400,
            timestamp: new Date().toISOString(),
          }
        };
      }
      return {
        ...state,
        selectedTechnologies: [...state.selectedTechnologies, action.payload],
        error: null,
      };
    
    case 'REMOVE_TECHNOLOGY':
      return {
        ...state,
        selectedTechnologies: state.selectedTechnologies.filter(tech => tech.id !== action.payload),
        // Clear comparison result if we have fewer than 2 technologies
        comparisonResult: state.selectedTechnologies.length <= 2 ? null : state.comparisonResult,
      };
    
    case 'CLEAR_TECHNOLOGIES':
      return {
        ...state,
        selectedTechnologies: [],
        comparisonResult: null,
      };
    
    case 'SET_TECHNOLOGIES':
      return {
        ...state,
        selectedTechnologies: action.payload.slice(0, state.maxTechnologies),
      };
    
    case 'UPDATE_CONSTRAINTS':
      return {
        ...state,
        userConstraints: {
          ...state.userConstraints,
          ...action.payload,
        },
      };
    
    case 'SET_COMPARISON_RESULT':
      return {
        ...state,
        comparisonResult: action.payload,
      };
    
    case 'SET_SESSION_ID':
      return {
        ...state,
        sessionId: action.payload,
      };
    
    case 'TOGGLE_ADVANCED_OPTIONS':
      return {
        ...state,
        showAdvancedOptions: !state.showAdvancedOptions,
      };
    
    case 'RESET_STATE':
      return {
        ...initialState,
        // Preserve some UI state
        showAdvancedOptions: state.showAdvancedOptions,
      };
    
    default:
      return state;
  }
}

// Context interface
interface ComparisonContextType {
  state: ComparisonState;
  
  // Technology management
  addTechnology: (technology: Technology) => void;
  removeTechnology: (technologyId: number) => void;
  clearTechnologies: () => void;
  setTechnologies: (technologies: Technology[]) => void;
  
  // Constraints management
  updateConstraints: (constraints: Partial<UserConstraints>) => void;
  addPriorityTag: (tag: string) => void;
  removePriorityTag: (tag: string) => void;
  
  // Comparison operations
  generateComparison: () => Promise<void>;
  saveComparison: (name?: string) => Promise<string | null>;
  loadComparison: (sessionId: string) => Promise<void>;
  
  // Utility functions
  canCompare: () => boolean;
  clearError: () => void;
  resetState: () => void;
  toggleAdvancedOptions: () => void;
}

// Create context
const ComparisonContext = createContext<ComparisonContextType | undefined>(undefined);

// Provider component
interface ComparisonProviderProps {
  children: React.ReactNode;
}

export function ComparisonProvider({ children }: ComparisonProviderProps) {
  const [state, dispatch] = useReducer(comparisonReducer, initialState);

  // Load state from localStorage on mount
  useEffect(() => {
    const savedState = localStorage.getItem('comparisonState');
    if (savedState) {
      try {
        const parsed = JSON.parse(savedState);
        if (parsed.selectedTechnologies) {
          dispatch({ type: 'SET_TECHNOLOGIES', payload: parsed.selectedTechnologies });
        }
        if (parsed.userConstraints) {
          dispatch({ type: 'UPDATE_CONSTRAINTS', payload: parsed.userConstraints });
        }
        if (parsed.sessionId) {
          dispatch({ type: 'SET_SESSION_ID', payload: parsed.sessionId });
        }
      } catch (error) {
        console.warn('Failed to load comparison state from localStorage:', error);
      }
    }
  }, []);

  // Save state to localStorage when it changes
  useEffect(() => {
    const stateToSave = {
      selectedTechnologies: state.selectedTechnologies,
      userConstraints: state.userConstraints,
      sessionId: state.sessionId,
    };
    localStorage.setItem('comparisonState', JSON.stringify(stateToSave));
  }, [state.selectedTechnologies, state.userConstraints, state.sessionId]);

  // Technology management functions
  const addTechnology = useCallback((technology: Technology) => {
    dispatch({ type: 'ADD_TECHNOLOGY', payload: technology });
  }, []);

  const removeTechnology = useCallback((technologyId: number) => {
    dispatch({ type: 'REMOVE_TECHNOLOGY', payload: technologyId });
  }, []);

  const clearTechnologies = useCallback(() => {
    dispatch({ type: 'CLEAR_TECHNOLOGIES' });
  }, []);

  const setTechnologies = useCallback((technologies: Technology[]) => {
    dispatch({ type: 'SET_TECHNOLOGIES', payload: technologies });
  }, []);

  // Constraints management functions
  const updateConstraints = useCallback((constraints: Partial<UserConstraints>) => {
    dispatch({ type: 'UPDATE_CONSTRAINTS', payload: constraints });
  }, []);

  const addPriorityTag = useCallback((tag: string) => {
    const currentTags = state.userConstraints.priorityTags;
    if (!currentTags.includes(tag)) {
      dispatch({
        type: 'UPDATE_CONSTRAINTS',
        payload: {
          priorityTags: [...currentTags, tag],
        },
      });
    }
  }, [state.userConstraints.priorityTags]);

  const removePriorityTag = useCallback((tag: string) => {
    const currentTags = state.userConstraints.priorityTags;
    dispatch({
      type: 'UPDATE_CONSTRAINTS',
      payload: {
        priorityTags: currentTags.filter(t => t !== tag),
      },
    });
  }, [state.userConstraints.priorityTags]);

  // Comparison operations
  const generateComparison = useCallback(async () => {
    if (state.selectedTechnologies.length < 2) {
      dispatch({
        type: 'SET_ERROR',
        payload: {
          message: 'At least 2 technologies are required for comparison',
          status: 400,
          timestamp: new Date().toISOString(),
        },
      });
      return;
    }

    dispatch({ type: 'SET_GENERATING_COMPARISON', payload: true });
    dispatch({ type: 'SET_ERROR', payload: null });

    try {
      const result = await comparisonService.detailedCompare(
        state.selectedTechnologies.map(tech => tech.id),
        state.userConstraints
      );

      dispatch({ type: 'SET_COMPARISON_RESULT', payload: result });
    } catch (error) {
      dispatch({ type: 'SET_ERROR', payload: error as ApiError });
    } finally {
      dispatch({ type: 'SET_GENERATING_COMPARISON', payload: false });
    }
  }, [state.selectedTechnologies, state.userConstraints]);

  const saveComparison = useCallback(async (name?: string): Promise<string | null> => {
    if (!state.comparisonResult) {
      return null;
    }

    try {
      const sessionId = await comparisonService.saveComparison(state.comparisonResult, name);
      dispatch({ type: 'SET_SESSION_ID', payload: sessionId });
      return sessionId;
    } catch (error) {
      dispatch({ type: 'SET_ERROR', payload: error as ApiError });
      return null;
    }
  }, [state.comparisonResult]);

  const loadComparison = useCallback(async (sessionId: string) => {
    dispatch({ type: 'SET_LOADING', payload: true });
    dispatch({ type: 'SET_ERROR', payload: null });

    try {
      const result = await comparisonService.getComparisonById(sessionId);
      dispatch({ type: 'SET_COMPARISON_RESULT', payload: result });
      dispatch({ type: 'SET_SESSION_ID', payload: sessionId });

      // Load technologies from the result
      if (result.scores.length > 0) {
        const technologies = result.scores.map(score => score.technology);
        dispatch({ type: 'SET_TECHNOLOGIES', payload: technologies });
      }

      // Load constraints if available
      if (result.constraints) {
        dispatch({ type: 'UPDATE_CONSTRAINTS', payload: result.constraints });
      }
    } catch (error) {
      dispatch({ type: 'SET_ERROR', payload: error as ApiError });
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  }, []);

  // Utility functions
  const canCompare = useCallback(() => {
    return state.selectedTechnologies.length >= 2 && !state.isGeneratingComparison;
  }, [state.selectedTechnologies.length, state.isGeneratingComparison]);

  const clearError = useCallback(() => {
    dispatch({ type: 'SET_ERROR', payload: null });
  }, []);

  const resetState = useCallback(() => {
    dispatch({ type: 'RESET_STATE' });
  }, []);

  const toggleAdvancedOptions = useCallback(() => {
    dispatch({ type: 'TOGGLE_ADVANCED_OPTIONS' });
  }, []);

  const contextValue: ComparisonContextType = {
    state,
    addTechnology,
    removeTechnology,
    clearTechnologies,
    setTechnologies,
    updateConstraints,
    addPriorityTag,
    removePriorityTag,
    generateComparison,
    saveComparison,
    loadComparison,
    canCompare,
    clearError,
    resetState,
    toggleAdvancedOptions,
  };

  return (
    <ComparisonContext.Provider value={contextValue}>
      {children}
    </ComparisonContext.Provider>
  );
}

// Hook to use the comparison context
export function useComparison() {
  const context = useContext(ComparisonContext);
  if (context === undefined) {
    throw new Error('useComparison must be used within a ComparisonProvider');
  }
  return context;
}
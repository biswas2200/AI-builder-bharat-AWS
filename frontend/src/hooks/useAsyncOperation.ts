import { useState, useCallback, useRef, useEffect } from 'react';
import { ApiError } from '../types/api';

interface UseAsyncOperationOptions {
  onSuccess?: (result: any) => void;
  onError?: (error: ApiError) => void;
  resetOnMount?: boolean;
}

interface UseAsyncOperationReturn<T> {
  // State
  data: T | null;
  isLoading: boolean;
  error: ApiError | null;
  
  // Functions
  execute: (operation: () => Promise<T>) => Promise<T | null>;
  reset: () => void;
  clearError: () => void;
  
  // Status checks
  isIdle: boolean;
  isSuccess: boolean;
  isError: boolean;
}

/**
 * Custom hook for managing async operations with loading states and error handling
 * Implements Requirements 8.5: loading states and error boundaries
 */
export function useAsyncOperation<T = any>(
  options: UseAsyncOperationOptions = {}
): UseAsyncOperationReturn<T> {
  const { onSuccess, onError, resetOnMount = true } = options;

  // State
  const [data, setData] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  // Ref to track if component is mounted
  const isMountedRef = useRef(true);

  // Reset on mount if requested
  useEffect(() => {
    if (resetOnMount) {
      reset();
    }
    
    return () => {
      isMountedRef.current = false;
    };
  }, [resetOnMount]);

  // Execute async operation
  const execute = useCallback(async (operation: () => Promise<T>): Promise<T | null> => {
    if (!isMountedRef.current) {
      return null;
    }

    setIsLoading(true);
    setError(null);

    try {
      const result = await operation();
      
      if (isMountedRef.current) {
        setData(result);
        onSuccess?.(result);
      }
      
      return result;
    } catch (err) {
      const apiError = err as ApiError;
      
      if (isMountedRef.current) {
        setError(apiError);
        onError?.(apiError);
      }
      
      return null;
    } finally {
      if (isMountedRef.current) {
        setIsLoading(false);
      }
    }
  }, [onSuccess, onError]);

  // Reset all state
  const reset = useCallback(() => {
    setData(null);
    setIsLoading(false);
    setError(null);
  }, []);

  // Clear error only
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  // Status checks
  const isIdle = !isLoading && !data && !error;
  const isSuccess = !isLoading && data !== null && !error;
  const isError = !isLoading && error !== null;

  return {
    data,
    isLoading,
    error,
    execute,
    reset,
    clearError,
    isIdle,
    isSuccess,
    isError,
  };
}

/**
 * Hook for managing multiple async operations
 */
export function useAsyncOperations() {
  const [operations, setOperations] = useState<Map<string, UseAsyncOperationReturn<any>>>(new Map());

  const createOperation = useCallback(<T>(
    key: string,
    options?: UseAsyncOperationOptions
  ): UseAsyncOperationReturn<T> => {
    const operation = useAsyncOperation<T>(options);
    
    setOperations(prev => new Map(prev.set(key, operation)));
    
    return operation;
  }, []);

  const getOperation = useCallback((key: string) => {
    return operations.get(key);
  }, [operations]);

  const removeOperation = useCallback((key: string) => {
    setOperations(prev => {
      const newMap = new Map(prev);
      newMap.delete(key);
      return newMap;
    });
  }, []);

  const resetAll = useCallback(() => {
    operations.forEach(operation => operation.reset());
  }, [operations]);

  const isAnyLoading = Array.from(operations.values()).some(op => op.isLoading);
  const hasAnyError = Array.from(operations.values()).some(op => op.error);

  return {
    createOperation,
    getOperation,
    removeOperation,
    resetAll,
    isAnyLoading,
    hasAnyError,
    operationCount: operations.size,
  };
}
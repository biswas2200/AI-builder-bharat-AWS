import { useState, useEffect, useCallback, useMemo } from 'react';
import { Technology, ApiError } from '../types/api';
import { inventoryService } from '../services';

interface UseTechnologySearchOptions {
  initialQuery?: string;
  autoSearch?: boolean;
  debounceMs?: number;
  minQueryLength?: number;
}

interface UseTechnologySearchReturn {
  // Search state
  query: string;
  results: Technology[];
  isSearching: boolean;
  error: ApiError | null;
  
  // Search functions
  setQuery: (query: string) => void;
  search: (query?: string) => Promise<void>;
  clearResults: () => void;
  clearError: () => void;
  
  // Utility functions
  hasResults: boolean;
  isEmpty: boolean;
  canSearch: boolean;
}

/**
 * Custom hook for technology search functionality
 * Implements Requirements 1.2: technology search and retrieval
 */
export function useTechnologySearch(options: UseTechnologySearchOptions = {}): UseTechnologySearchReturn {
  const {
    initialQuery = '',
    autoSearch = true,
    debounceMs = 300,
    minQueryLength = 2,
  } = options;

  // State
  const [query, setQuery] = useState(initialQuery);
  const [results, setResults] = useState<Technology[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  // Debounced search function
  const debouncedSearch = useCallback(
    debounce(async (searchQuery: string) => {
      if (searchQuery.length < minQueryLength) {
        setResults([]);
        return;
      }

      setIsSearching(true);
      setError(null);

      try {
        const searchResults = await inventoryService.searchTechnologies(searchQuery);
        setResults(searchResults);
      } catch (err) {
        setError(err as ApiError);
        setResults([]);
      } finally {
        setIsSearching(false);
      }
    }, debounceMs),
    [minQueryLength, debounceMs]
  );

  // Auto search when query changes
  useEffect(() => {
    if (autoSearch && query.trim()) {
      debouncedSearch(query.trim());
    } else if (!query.trim()) {
      setResults([]);
    }
  }, [query, autoSearch, debouncedSearch]);

  // Manual search function
  const search = useCallback(async (searchQuery?: string) => {
    const queryToSearch = searchQuery ?? query;
    
    if (queryToSearch.length < minQueryLength) {
      setError({
        message: `Query must be at least ${minQueryLength} characters`,
        status: 400,
        timestamp: new Date().toISOString(),
      });
      return;
    }

    setIsSearching(true);
    setError(null);

    try {
      const searchResults = await inventoryService.searchTechnologies(queryToSearch);
      setResults(searchResults);
    } catch (err) {
      setError(err as ApiError);
      setResults([]);
    } finally {
      setIsSearching(false);
    }
  }, [query, minQueryLength]);

  // Clear functions
  const clearResults = useCallback(() => {
    setResults([]);
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  // Computed values
  const hasResults = useMemo(() => results.length > 0, [results.length]);
  const isEmpty = useMemo(() => results.length === 0 && !isSearching, [results.length, isSearching]);
  const canSearch = useMemo(() => query.length >= minQueryLength, [query.length, minQueryLength]);

  return {
    query,
    results,
    isSearching,
    error,
    setQuery,
    search,
    clearResults,
    clearError,
    hasResults,
    isEmpty,
    canSearch,
  };
}

/**
 * Debounce utility function
 */
function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout;
  
  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
}
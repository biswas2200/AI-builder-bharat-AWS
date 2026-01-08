import apiClient, { handleApiResponse, createQueryParams } from './apiClient';
import {
  Technology,
  Criteria,
  CriteriaType,
  CreateTechnologyRequest,
  UpdateTechnologyRequest,
  CreateCriteriaRequest,
} from '../types/api';

/**
 * Inventory Service - Handles technology and criteria management
 * Implements Requirements 1.2, 1.4, 1.5
 */
export class InventoryService {
  
  // Technology endpoints
  
  /**
   * Get all technologies
   */
  async getAllTechnologies(): Promise<Technology[]> {
    const response = await apiClient.get<Technology[]>('/inventory/technologies');
    return handleApiResponse(response);
  }

  /**
   * Get technology by ID
   */
  async getTechnologyById(id: number): Promise<Technology> {
    const response = await apiClient.get<Technology>(`/inventory/technologies/${id}`);
    return handleApiResponse(response);
  }

  /**
   * Search technologies by query string
   * Implements Requirement 1.2: technology search functionality
   */
  async searchTechnologies(query: string): Promise<Technology[]> {
    if (!query.trim()) {
      return [];
    }
    
    const queryParams = createQueryParams({ query: query.trim() });
    const response = await apiClient.get<Technology[]>(`/inventory/technologies/search?${queryParams}`);
    return handleApiResponse(response);
  }

  /**
   * Get technologies by category
   */
  async getTechnologiesByCategory(category: string): Promise<Technology[]> {
    const response = await apiClient.get<Technology[]>(`/inventory/technologies/category/${encodeURIComponent(category)}`);
    return handleApiResponse(response);
  }

  /**
   * Get technologies by tag
   */
  async getTechnologiesByTag(tag: string): Promise<Technology[]> {
    const response = await apiClient.get<Technology[]>(`/inventory/technologies/tag/${encodeURIComponent(tag)}`);
    return handleApiResponse(response);
  }

  /**
   * Create a new custom technology
   * Implements Requirement 1.5: custom technology creation
   */
  async createTechnology(request: CreateTechnologyRequest): Promise<Technology> {
    const response = await apiClient.post<Technology>('/inventory/technologies', request);
    return handleApiResponse(response);
  }

  /**
   * Update an existing technology
   */
  async updateTechnology(id: number, request: UpdateTechnologyRequest): Promise<Technology> {
    const response = await apiClient.put<Technology>(`/inventory/technologies/${id}`, request);
    return handleApiResponse(response);
  }

  /**
   * Delete a technology
   */
  async deleteTechnology(id: number): Promise<void> {
    await apiClient.delete(`/inventory/technologies/${id}`);
  }

  /**
   * Get all available categories
   */
  async getAllCategories(): Promise<string[]> {
    const response = await apiClient.get<string[]>('/inventory/categories');
    return handleApiResponse(response);
  }

  /**
   * Get all available tags
   */
  async getAllTags(): Promise<string[]> {
    const response = await apiClient.get<string[]>('/inventory/tags');
    return handleApiResponse(response);
  }

  // Criteria endpoints

  /**
   * Get all criteria
   */
  async getAllCriteria(): Promise<Criteria[]> {
    const response = await apiClient.get<Criteria[]>('/inventory/criteria');
    return handleApiResponse(response);
  }

  /**
   * Get criteria by ID
   */
  async getCriteriaById(id: number): Promise<Criteria> {
    const response = await apiClient.get<Criteria>(`/inventory/criteria/${id}`);
    return handleApiResponse(response);
  }

  /**
   * Get criteria by type
   */
  async getCriteriaByType(type: CriteriaType): Promise<Criteria[]> {
    const response = await apiClient.get<Criteria[]>(`/inventory/criteria/type/${type}`);
    return handleApiResponse(response);
  }

  /**
   * Create a new custom criteria
   */
  async createCriteria(request: CreateCriteriaRequest): Promise<Criteria> {
    const response = await apiClient.post<Criteria>('/inventory/criteria', request);
    return handleApiResponse(response);
  }

  /**
   * Delete a criteria
   */
  async deleteCriteria(id: number): Promise<void> {
    await apiClient.delete(`/inventory/criteria/${id}`);
  }

  // Utility methods

  /**
   * Get multiple technologies by IDs
   */
  async getTechnologiesByIds(ids: number[]): Promise<Technology[]> {
    if (ids.length === 0) {
      return [];
    }

    // Fetch technologies in parallel
    const promises = ids.map(id => this.getTechnologyById(id));
    const results = await Promise.allSettled(promises);
    
    // Filter out failed requests and return successful ones
    return results
      .filter((result): result is PromiseFulfilledResult<Technology> => result.status === 'fulfilled')
      .map(result => result.value);
  }

  /**
   * Search technologies with advanced filters
   */
  async searchTechnologiesAdvanced(filters: {
    query?: string;
    category?: string;
    tags?: string[];
    limit?: number;
  }): Promise<Technology[]> {
    let technologies: Technology[] = [];

    if (filters.query) {
      technologies = await this.searchTechnologies(filters.query);
    } else {
      technologies = await this.getAllTechnologies();
    }

    // Apply client-side filtering for additional criteria
    let filtered = technologies;

    if (filters.category) {
      filtered = filtered.filter(tech => 
        tech.category.toLowerCase() === filters.category!.toLowerCase()
      );
    }

    if (filters.tags && filters.tags.length > 0) {
      const lowerTags = filters.tags.map(tag => tag.toLowerCase());
      filtered = filtered.filter(tech =>
        tech.tags.some(tag => lowerTags.includes(tag.toLowerCase()))
      );
    }

    if (filters.limit && filters.limit > 0) {
      filtered = filtered.slice(0, filters.limit);
    }

    return filtered;
  }

  /**
   * Validate technology data before creation/update
   */
  validateTechnologyData(data: CreateTechnologyRequest | UpdateTechnologyRequest): string[] {
    const errors: string[] = [];

    if ('name' in data && data.name) {
      if (data.name.trim().length === 0) {
        errors.push('Technology name is required');
      } else if (data.name.length > 100) {
        errors.push('Technology name must not exceed 100 characters');
      }
    }

    if ('category' in data && data.category) {
      if (data.category.trim().length === 0) {
        errors.push('Category is required');
      } else if (data.category.length > 50) {
        errors.push('Category must not exceed 50 characters');
      }
    }

    if (data.metrics) {
      Object.entries(data.metrics).forEach(([key, value]) => {
        if (typeof value !== 'number' || isNaN(value)) {
          errors.push(`Metric '${key}' must be a valid number`);
        }
      });
    }

    return errors;
  }
}

// Export singleton instance
export const inventoryService = new InventoryService();
import apiClient, { handleApiResponse } from './apiClient';
import {
  ComparisonResult,
  ComparisonRequest,
  UserConstraints,
  Technology,
} from '../types/api';

/**
 * Comparison Service - Handles technology comparison and AI insights
 * Implements Requirements 2.1, 4.2
 */
export class ComparisonService {

  /**
   * Perform technology comparison with weighted scoring
   * Implements Requirement 2.1: weighted scoring algorithm
   */
  async compareTechnologies(request: ComparisonRequest): Promise<ComparisonResult> {
    const response = await apiClient.post<ComparisonResult>('/referee/compare', request);
    return handleApiResponse(response);
  }

  /**
   * Get comparison result by session ID (if sessions are implemented)
   */
  async getComparisonById(sessionId: string): Promise<ComparisonResult> {
    const response = await apiClient.get<ComparisonResult>(`/referee/comparison/${sessionId}`);
    return handleApiResponse(response);
  }

  /**
   * Generate AI insights for specific technologies
   * Implements Requirement 4.2: AI-powered recommendations
   */
  async generateInsights(technologyNames: string[]): Promise<ComparisonResult> {
    const response = await apiClient.post<ComparisonResult>('/insight/generate', {
      technologies: technologyNames
    });
    return handleApiResponse(response);
  }

  /**
   * Get personalized recommendations based on user constraints
   * Implements Requirement 4.2: personalized recommendations
   */
  async getPersonalizedRecommendations(
    technologies: Technology[],
    constraints: UserConstraints
  ): Promise<string> {
    const response = await apiClient.post<{ recommendation: string }>('/insight/recommendations', {
      technologies: technologies.map(t => t.name),
      constraints
    });
    return handleApiResponse(response).recommendation;
  }

  /**
   * Perform quick comparison with minimal data
   * Useful for preview comparisons or when full comparison isn't needed
   */
  async quickCompare(technologyIds: number[], priorityTags: string[] = []): Promise<ComparisonResult> {
    const constraints: UserConstraints = {
      priorityTags,
    };

    return this.compareTechnologies({
      technologyIds,
      constraints
    });
  }

  /**
   * Compare technologies with detailed constraints
   */
  async detailedCompare(
    technologyIds: number[],
    constraints: UserConstraints
  ): Promise<ComparisonResult> {
    // Validate input
    if (technologyIds.length < 2) {
      throw new Error('At least 2 technologies are required for comparison');
    }

    if (technologyIds.length > 5) {
      throw new Error('Maximum 5 technologies can be compared at once');
    }

    return this.compareTechnologies({
      technologyIds,
      constraints
    });
  }

  /**
   * Get historical comparison data (if available)
   */
  async getHistoricalComparisons(userId?: string): Promise<ComparisonResult[]> {
    const endpoint = userId 
      ? `/referee/history?userId=${encodeURIComponent(userId)}`
      : '/referee/history';
    
    const response = await apiClient.get<ComparisonResult[]>(endpoint);
    return handleApiResponse(response);
  }

  /**
   * Save comparison result for later retrieval
   */
  async saveComparison(result: ComparisonResult, name?: string): Promise<string> {
    const response = await apiClient.post<{ sessionId: string }>('/referee/save', {
      result,
      name
    });
    return handleApiResponse(response).sessionId;
  }

  /**
   * Export comparison result in different formats
   */
  async exportComparison(
    result: ComparisonResult, 
    format: 'json' | 'csv' | 'pdf' = 'json'
  ): Promise<Blob> {
    const response = await apiClient.post('/referee/export', {
      result,
      format
    }, {
      responseType: 'blob'
    });
    return response.data;
  }

  // Utility methods

  /**
   * Validate comparison request
   */
  validateComparisonRequest(request: ComparisonRequest): string[] {
    const errors: string[] = [];

    if (!request.technologyIds || request.technologyIds.length === 0) {
      errors.push('At least one technology ID is required');
    } else if (request.technologyIds.length < 2) {
      errors.push('At least 2 technologies are required for comparison');
    } else if (request.technologyIds.length > 5) {
      errors.push('Maximum 5 technologies can be compared at once');
    }

    // Check for duplicate IDs
    const uniqueIds = new Set(request.technologyIds);
    if (uniqueIds.size !== request.technologyIds.length) {
      errors.push('Duplicate technology IDs are not allowed');
    }

    if (!request.constraints) {
      errors.push('User constraints are required');
    } else {
      if (!Array.isArray(request.constraints.priorityTags)) {
        errors.push('Priority tags must be an array');
      }
    }

    return errors;
  }

  /**
   * Create default user constraints
   */
  createDefaultConstraints(): UserConstraints {
    return {
      priorityTags: [],
      projectType: undefined,
      teamSize: undefined,
      timeline: undefined
    };
  }

  /**
   * Merge user constraints with defaults
   */
  mergeConstraints(partial: Partial<UserConstraints>): UserConstraints {
    const defaults = this.createDefaultConstraints();
    return {
      ...defaults,
      ...partial,
      priorityTags: partial.priorityTags || defaults.priorityTags
    };
  }

  /**
   * Calculate comparison score difference between technologies
   */
  calculateScoreDifference(result: ComparisonResult): Record<string, number> {
    if (result.scores.length < 2) {
      return {};
    }

    const differences: Record<string, number> = {};
    const sortedScores = [...result.scores].sort((a, b) => b.overallScore - a.overallScore);
    
    for (let i = 1; i < sortedScores.length; i++) {
      const current = sortedScores[i];
      const previous = sortedScores[i - 1];
      const diff = previous.overallScore - current.overallScore;
      differences[current.technology.name] = Math.round(diff * 100) / 100;
    }

    return differences;
  }

  /**
   * Get top performing technology from comparison result
   */
  getTopTechnology(result: ComparisonResult): Technology | null {
    if (result.scores.length === 0) {
      return null;
    }

    const topScore = result.scores.reduce((max, current) => 
      current.overallScore > max.overallScore ? current : max
    );

    return topScore.technology;
  }

  /**
   * Filter comparison results by minimum score threshold
   */
  filterByMinScore(result: ComparisonResult, minScore: number): ComparisonResult {
    const filteredScores = result.scores.filter(score => score.overallScore >= minScore);
    
    return {
      ...result,
      scores: filteredScores
    };
  }
}

// Export singleton instance
export const comparisonService = new ComparisonService();
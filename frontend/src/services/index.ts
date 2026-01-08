// API Services - Centralized exports
export { default as apiClient, handleApiResponse, createQueryParams } from './apiClient';
export { InventoryService, inventoryService } from './inventoryService';
export { ComparisonService, comparisonService } from './comparisonService';

// Re-export types for convenience
export type {
  Technology,
  Criteria,
  CriteriaType,
  UserConstraints,
  TechnologyScore,
  RadarChartData,
  KpiMetric,
  KpiMetricType,
  ComparisonResult,
  CreateTechnologyRequest,
  UpdateTechnologyRequest,
  CreateCriteriaRequest,
  ComparisonRequest,
  ApiResponse,
  ApiError,
} from '../types/api';
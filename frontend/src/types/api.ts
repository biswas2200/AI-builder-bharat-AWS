// API Types - TypeScript interfaces matching backend domain models

export interface Technology {
  id: number;
  name: string;
  category: string;
  description?: string;
  metrics: Record<string, number>;
  tags: string[];
  createdAt: string;
}

export interface Criteria {
  id: number;
  name: string;
  description?: string;
  weight: number;
  type: CriteriaType;
}

export enum CriteriaType {
  PERFORMANCE = 'PERFORMANCE',
  LEARNING_CURVE = 'LEARNING_CURVE',
  COMMUNITY = 'COMMUNITY',
  DOCUMENTATION = 'DOCUMENTATION',
  SCALABILITY = 'SCALABILITY',
  SECURITY = 'SECURITY',
  COST = 'COST',
  MAINTENANCE = 'MAINTENANCE'
}

export interface UserConstraints {
  priorityTags: string[];
  projectType?: string;
  teamSize?: string;
  timeline?: string;
}

export interface TechnologyScore {
  technology: Technology;
  overallScore: number;
  criterionScores: Record<string, number>;
  explanation?: string;
}

export interface RadarChartData {
  subject: string;
  A: number;
  B?: number;
  C?: number;
  D?: number;
  E?: number;
  fullMark: number;
}

export interface KpiMetric {
  name: string;
  value: any;
  displayValue: string;
  unit?: string;
  description?: string;
  type: KpiMetricType;
}

export enum KpiMetricType {
  NUMERIC = 'NUMERIC',
  PERCENTAGE = 'PERCENTAGE',
  RATING = 'RATING',
  COUNT = 'COUNT',
  TREND = 'TREND',
  CATEGORICAL = 'CATEGORICAL'
}

export interface ComparisonResult {
  scores: TechnologyScore[];
  radarData: RadarChartData[];
  kpiMetrics: Record<string, KpiMetric[]>;
  recommendationSummary?: string;
  generatedAt: string;
  constraints?: UserConstraints;
}

// Request DTOs
export interface CreateTechnologyRequest {
  name: string;
  category: string;
  description?: string;
  metrics?: Record<string, number>;
  tags?: string[];
}

export interface UpdateTechnologyRequest {
  name?: string;
  category?: string;
  description?: string;
  metrics?: Record<string, number>;
  tags?: string[];
}

export interface CreateCriteriaRequest {
  name: string;
  description?: string;
  weight: number;
  type: CriteriaType;
}

export interface ComparisonRequest {
  technologyIds: number[];
  constraints: UserConstraints;
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
}

// Error response
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path?: string;
}
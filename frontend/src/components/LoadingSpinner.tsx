import React from 'react';
import { Loader2 } from 'lucide-react';

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg' | 'xl';
  text?: string;
  className?: string;
  fullScreen?: boolean;
}

/**
 * Loading Spinner component for consistent loading states
 * Implements Requirements 8.5: loading states
 */
export function LoadingSpinner({ 
  size = 'md', 
  text, 
  className = '', 
  fullScreen = false 
}: LoadingSpinnerProps) {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8',
    xl: 'w-12 h-12',
  };

  const textSizeClasses = {
    sm: 'text-sm',
    md: 'text-base',
    lg: 'text-lg',
    xl: 'text-xl',
  };

  const spinner = (
    <div className={`flex items-center justify-center ${className}`}>
      <div className="flex flex-col items-center space-y-2">
        <Loader2 
          className={`${sizeClasses[size]} animate-spin text-purple-600 dark:text-purple-400`} 
        />
        {text && (
          <p className={`${textSizeClasses[size]} text-gray-600 dark:text-gray-300 animate-pulse`}>
            {text}
          </p>
        )}
      </div>
    </div>
  );

  if (fullScreen) {
    return (
      <div className="fixed inset-0 bg-white/80 dark:bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center">
        {spinner}
      </div>
    );
  }

  return spinner;
}

/**
 * Inline loading spinner for buttons and small spaces
 */
export function InlineSpinner({ size = 'sm', className = '' }: Pick<LoadingSpinnerProps, 'size' | 'className'>) {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-5 h-5',
    lg: 'w-6 h-6',
    xl: 'w-8 h-8',
  };

  return (
    <Loader2 
      className={`${sizeClasses[size]} animate-spin text-current ${className}`} 
    />
  );
}

/**
 * Loading overlay for specific components
 */
export function LoadingOverlay({ 
  isLoading, 
  children, 
  text = 'Loading...',
  className = '' 
}: {
  isLoading: boolean;
  children: React.ReactNode;
  text?: string;
  className?: string;
}) {
  return (
    <div className={`relative ${className}`}>
      {children}
      {isLoading && (
        <div className="absolute inset-0 bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm flex items-center justify-center rounded-lg">
          <LoadingSpinner text={text} />
        </div>
      )}
    </div>
  );
}

/**
 * Skeleton loader for content placeholders
 */
export function SkeletonLoader({ 
  lines = 3, 
  className = '',
  animated = true 
}: {
  lines?: number;
  className?: string;
  animated?: boolean;
}) {
  return (
    <div className={`space-y-3 ${className}`}>
      {Array.from({ length: lines }).map((_, index) => (
        <div
          key={index}
          className={`h-4 bg-gray-200 dark:bg-slate-700 rounded ${
            animated ? 'animate-pulse' : ''
          }`}
          style={{
            width: `${Math.random() * 40 + 60}%`,
          }}
        />
      ))}
    </div>
  );
}
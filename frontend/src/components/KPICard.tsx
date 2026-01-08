import { Star, TrendingUp, Users, Download, Briefcase, Heart } from 'lucide-react'

interface KPIMetric {
  label: string
  value: string | number
  icon: 'star' | 'trending' | 'users' | 'download' | 'briefcase' | 'heart'
  trend?: number
}

interface KPICardProps {
  technology: string
  metrics: KPIMetric[]
  overallScore: number
}

const iconMap = {
  star: Star,
  trending: TrendingUp,
  users: Users,
  download: Download,
  briefcase: Briefcase,
  heart: Heart
}

export default function KPICard({ technology, metrics, overallScore }: KPICardProps) {
  return (
    <div className="void-card rounded-xl p-6 glow-on-hover">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-12 h-12 rounded-xl bg-purple-100 dark:bg-purple-900/50 flex items-center justify-center">
          <span className="text-purple-600 dark:text-purple-400 font-bold text-lg">
            {technology.charAt(0).toUpperCase()}
          </span>
        </div>
        <div>
          <h5 className="text-lg font-semibold text-purple-800 dark:text-purple-200">
            {technology}
          </h5>
          <div className="flex items-center gap-2">
            <span className="text-sm text-purple-600 dark:text-purple-400">Overall Score:</span>
            <span className="text-xl font-bold neon-text">
              {overallScore}
            </span>
          </div>
        </div>
      </div>
      
      <div className="space-y-4">
        {metrics.map((metric, index) => {
          const IconComponent = iconMap[metric.icon]
          return (
            <div key={index} className="flex items-center justify-between p-3 rounded-lg glassmorphism">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-lg bg-purple-50 dark:bg-purple-900/30">
                  <IconComponent className="w-4 h-4 text-purple-600 dark:text-purple-400" />
                </div>
                <span className="text-sm font-medium text-purple-700 dark:text-purple-300">
                  {metric.label}
                </span>
              </div>
              <div className="text-right">
                <div className="text-sm font-bold text-purple-800 dark:text-purple-200">
                  {metric.value}
                </div>
                {metric.trend && (
                  <div className={`text-xs flex items-center gap-1 ${
                    metric.trend > 0 
                      ? 'text-green-600 dark:text-green-400' 
                      : 'text-red-600 dark:text-red-400'
                  }`}>
                    <TrendingUp className={`w-3 h-3 ${metric.trend < 0 ? 'rotate-180' : ''}`} />
                    {Math.abs(metric.trend)}%
                  </div>
                )}
              </div>
            </div>
          )
        })}
      </div>
      
      {/* Score Bar */}
      <div className="mt-6">
        <div className="flex justify-between items-center mb-2">
          <span className="text-sm font-medium text-purple-700 dark:text-purple-300">
            Performance Score
          </span>
          <span className="text-sm font-bold text-purple-800 dark:text-purple-200">
            {overallScore}/100
          </span>
        </div>
        <div className="w-full bg-purple-100 dark:bg-purple-900/30 rounded-full h-2">
          <div 
            className="bg-gradient-to-r from-purple-500 to-purple-600 h-2 rounded-full transition-all duration-500"
            style={{ width: `${overallScore}%` }}
          />
        </div>
      </div>
    </div>
  )
}
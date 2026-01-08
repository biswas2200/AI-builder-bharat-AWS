import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Settings, Filter, Star, TrendingUp, Users, Clock, Plus, X } from 'lucide-react'
import RadarChart from '../components/RadarChart'
import TrendChart from '../components/TrendChart'
import KPICard from '../components/KPICard'

export default function ComparisonDashboard() {
  const [searchParams] = useSearchParams()
  const initialQuery = searchParams.get('q') || ''
  
  const [selectedTechnologies, setSelectedTechnologies] = useState<string[]>(
    initialQuery ? initialQuery.split(',').map(t => t.trim()) : []
  )
  const [priorityTags, setPriorityTags] = useState<string[]>(['performance'])
  
  const availableTags = [
    { id: 'performance', label: 'Performance', icon: <TrendingUp className="w-4 h-4" /> },
    { id: 'learning-curve', label: 'Learning Curve', icon: <Clock className="w-4 h-4" /> },
    { id: 'community', label: 'Community', icon: <Users className="w-4 h-4" /> },
    { id: 'popularity', label: 'Popularity', icon: <Star className="w-4 h-4" /> },
  ]

  // Mock data for demonstration
  const radarData = [
    { subject: 'Performance', A: 85, B: 78, C: 92, fullMark: 100 },
    { subject: 'Learning Curve', A: 70, B: 85, C: 65, fullMark: 100 },
    { subject: 'Community', A: 95, B: 88, C: 90, fullMark: 100 },
    { subject: 'Documentation', A: 88, B: 92, C: 85, fullMark: 100 },
    { subject: 'Ecosystem', A: 90, B: 75, C: 88, fullMark: 100 },
    { subject: 'Job Market', A: 92, B: 80, C: 85, fullMark: 100 },
  ]

  const trendData = [
    { month: 'Jan', react: 85, vue: 78, angular: 72 },
    { month: 'Feb', react: 87, vue: 80, angular: 74 },
    { month: 'Mar', react: 89, vue: 82, angular: 75 },
    { month: 'Apr', react: 91, vue: 84, angular: 76 },
    { month: 'May', react: 93, vue: 86, angular: 78 },
    { month: 'Jun', react: 95, vue: 88, angular: 80 },
  ]

  const mockKPIData = [
    {
      technology: 'React',
      overallScore: 89,
      metrics: [
        { label: 'GitHub Stars', value: '220k', icon: 'star' as const, trend: 5 },
        { label: 'NPM Downloads', value: '18M/week', icon: 'download' as const, trend: 12 },
        { label: 'Job Openings', value: '15.2k', icon: 'briefcase' as const, trend: 8 },
        { label: 'Developer Satisfaction', value: '87%', icon: 'heart' as const, trend: 3 },
      ]
    },
    {
      technology: 'Vue',
      overallScore: 82,
      metrics: [
        { label: 'GitHub Stars', value: '206k', icon: 'star' as const, trend: 7 },
        { label: 'NPM Downloads', value: '4.2M/week', icon: 'download' as const, trend: 15 },
        { label: 'Job Openings', value: '8.5k', icon: 'briefcase' as const, trend: 12 },
        { label: 'Developer Satisfaction', value: '92%', icon: 'heart' as const, trend: 5 },
      ]
    },
    {
      technology: 'Angular',
      overallScore: 78,
      metrics: [
        { label: 'GitHub Stars', value: '93k', icon: 'star' as const, trend: 2 },
        { label: 'NPM Downloads', value: '3.1M/week', icon: 'download' as const, trend: 8 },
        { label: 'Job Openings', value: '12.8k', icon: 'briefcase' as const, trend: 6 },
        { label: 'Developer Satisfaction', value: '73%', icon: 'heart' as const, trend: -2 },
      ]
    }
  ]

  const togglePriorityTag = (tagId: string) => {
    setPriorityTags(prev => 
      prev.includes(tagId) 
        ? prev.filter(t => t !== tagId)
        : [...prev, tagId]
    )
  }

  const removeTechnology = (tech: string) => {
    setSelectedTechnologies(prev => prev.filter(t => t !== tech))
  }

  return (
    <div className="min-h-screen bg-purple-50 dark:bg-slate-950">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold neon-text mb-2">
            Technology Comparison Dashboard
          </h1>
          <p className="text-purple-600 dark:text-purple-400">
            Compare technologies and get AI-powered insights for your project
          </p>
        </div>
        
        <div className="grid lg:grid-cols-4 gap-8">
          {/* Sidebar - The Referee */}
          <div className="lg:col-span-1">
            <div className="void-card rounded-xl p-6 sticky top-24 space-y-6">
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 rounded-lg bg-purple-100 dark:bg-purple-900/50">
                  <Settings className="w-5 h-5 text-purple-600 dark:text-purple-400" />
                </div>
                <h2 className="text-xl font-semibold neon-text">
                  The Referee
                </h2>
              </div>
              
              {/* Selected Technologies */}
              <div>
                <h3 className="text-sm font-medium text-purple-700 dark:text-purple-300 mb-3 flex items-center gap-2">
                  <Filter className="w-4 h-4" />
                  Selected Technologies
                </h3>
                <div className="space-y-2">
                  {selectedTechnologies.length > 0 ? (
                    selectedTechnologies.map((tech) => (
                      <div
                        key={tech}
                        className="flex items-center justify-between p-3 rounded-lg glassmorphism neon-border group"
                      >
                        <span className="text-purple-800 dark:text-purple-200 font-medium">
                          {tech}
                        </span>
                        <button
                          onClick={() => removeTechnology(tech)}
                          className="opacity-0 group-hover:opacity-100 p-1 rounded text-purple-500 hover:text-purple-700 dark:hover:text-purple-300 transition-all"
                        >
                          <X className="w-4 h-4" />
                        </button>
                      </div>
                    ))
                  ) : (
                    <div className="p-4 rounded-lg glassmorphism text-center">
                      <p className="text-purple-600 dark:text-purple-400 text-sm">
                        No technologies selected
                      </p>
                    </div>
                  )}
                  
                  <button className="w-full p-3 rounded-lg glassmorphism neon-border hover:bg-purple-50 dark:hover:bg-purple-900/20 transition-all duration-300 flex items-center justify-center gap-2 text-purple-600 dark:text-purple-400">
                    <Plus className="w-4 h-4" />
                    Add Technology
                  </button>
                </div>
              </div>
              
              {/* Priority Tags */}
              <div>
                <h3 className="text-sm font-medium text-purple-700 dark:text-purple-300 mb-3">
                  Priority Criteria (1.5x weight)
                </h3>
                <div className="space-y-2">
                  {availableTags.map((tag) => (
                    <button
                      key={tag.id}
                      onClick={() => togglePriorityTag(tag.id)}
                      className={`w-full p-3 rounded-lg transition-all duration-300 flex items-center gap-3 ${
                        priorityTags.includes(tag.id)
                          ? 'bg-purple-100 dark:bg-purple-900/50 neon-border text-purple-800 dark:text-purple-200'
                          : 'glassmorphism hover:bg-purple-50 dark:hover:bg-purple-900/20 text-purple-600 dark:text-purple-400'
                      }`}
                    >
                      {tag.icon}
                      <span className="text-sm font-medium">{tag.label}</span>
                    </button>
                  ))}
                </div>
              </div>
              
              {/* Compare Button */}
              <button className="w-full p-4 rounded-xl bg-purple-600 hover:bg-purple-700 dark:bg-purple-500 dark:hover:bg-purple-600 text-white font-semibold transition-all duration-300 glow-on-hover">
                Generate Comparison
              </button>
            </div>
          </div>
          
          {/* Main Content */}
          <div className="lg:col-span-3 space-y-8">
            {selectedTechnologies.length > 0 ? (
              <>
                {/* Comparison Results Header */}
                <div className="void-card rounded-xl p-6">
                  <h3 className="text-2xl font-semibold neon-text mb-2">
                    Comparison Results
                  </h3>
                  <p className="text-purple-600 dark:text-purple-400">
                    Analyzing {selectedTechnologies.join(', ')} based on your selected criteria
                  </p>
                </div>
                
                {/* Radar Chart */}
                <div className="void-card rounded-xl p-8 glow-on-hover">
                  <h4 className="text-xl font-semibold text-purple-800 dark:text-purple-200 mb-6">
                    Multi-Dimensional Analysis
                  </h4>
                  <RadarChart 
                    data={radarData} 
                    technologies={selectedTechnologies.slice(0, 3)} 
                  />
                </div>
                
                {/* KPI Cards Grid */}
                <div className="grid md:grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
                  {selectedTechnologies.slice(0, 3).map((tech) => {
                    const kpiData = mockKPIData.find(d => d.technology.toLowerCase() === tech.toLowerCase())
                    return kpiData ? (
                      <KPICard
                        key={tech}
                        technology={kpiData.technology}
                        metrics={kpiData.metrics}
                        overallScore={kpiData.overallScore}
                      />
                    ) : null
                  })}
                </div>
                
                {/* Trend Charts */}
                <div className="grid md:grid-cols-1 lg:grid-cols-2 gap-6">
                  <TrendChart
                    data={trendData}
                    technologies={selectedTechnologies.slice(0, 3)}
                    title="Popularity Trends (6 months)"
                  />
                  <TrendChart
                    data={trendData.map(d => ({
                      ...d,
                      react: d.react * 0.9,
                      vue: d.vue * 1.1,
                      angular: d.angular * 0.95
                    }))}
                    technologies={selectedTechnologies.slice(0, 3)}
                    title="Job Market Trends"
                  />
                </div>
                
                {/* AI Insights */}
                <div className="void-card rounded-xl p-8 glow-on-hover">
                  <h4 className="text-xl font-semibold text-purple-800 dark:text-purple-200 mb-6 flex items-center gap-3">
                    <div className="p-2 rounded-lg bg-purple-100 dark:bg-purple-900/50">
                      <Star className="w-5 h-5 text-purple-600 dark:text-purple-400" />
                    </div>
                    AI-Powered Insights
                  </h4>
                  <div className="space-y-4">
                    <div className="p-4 rounded-lg glassmorphism">
                      <h5 className="font-semibold text-purple-800 dark:text-purple-200 mb-2">
                        Recommendation
                      </h5>
                      <p className="text-purple-600 dark:text-purple-400 text-sm">
                        Based on your priority criteria, React shows the strongest performance in job market demand and ecosystem maturity, making it ideal for enterprise projects with long-term maintenance requirements.
                      </p>
                    </div>
                    <div className="p-4 rounded-lg glassmorphism">
                      <h5 className="font-semibold text-purple-800 dark:text-purple-200 mb-2">
                        Key Trade-offs
                      </h5>
                      <p className="text-purple-600 dark:text-purple-400 text-sm">
                        Vue offers the highest developer satisfaction but lower job market presence. Angular provides enterprise features but has a steeper learning curve. Consider team experience and project timeline when making your decision.
                      </p>
                    </div>
                  </div>
                </div>
              </>
            ) : (
              /* Empty State */
              <div className="void-card rounded-xl p-12 text-center">
                <div className="w-16 h-16 mx-auto mb-6 rounded-full bg-purple-100 dark:bg-purple-900/50 flex items-center justify-center">
                  <TrendingUp className="w-8 h-8 text-purple-600 dark:text-purple-400" />
                </div>
                <h3 className="text-xl font-semibold text-purple-800 dark:text-purple-200 mb-4">
                  Ready to Compare Technologies?
                </h3>
                <p className="text-purple-600 dark:text-purple-400 mb-6 max-w-md mx-auto">
                  Select technologies from the sidebar to start your comparison and get AI-powered insights.
                </p>
                <button className="px-6 py-3 rounded-xl bg-purple-600 hover:bg-purple-700 dark:bg-purple-500 dark:hover:bg-purple-600 text-white font-semibold transition-all duration-300 glow-on-hover">
                  Add Technologies
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
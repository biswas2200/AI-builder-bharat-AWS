import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Search, ArrowRight, Zap, BarChart3, Brain } from 'lucide-react'

export default function LandingPage() {
  const [searchQuery, setSearchQuery] = useState('')
  const navigate = useNavigate()

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (searchQuery.trim()) {
      navigate(`/compare?q=${encodeURIComponent(searchQuery)}`)
    }
  }

  const exampleComparisons = [
    { 
      title: 'Frontend Frameworks', 
      techs: ['React', 'Vue', 'Angular'],
      icon: <Zap className="w-5 h-5" />,
      description: 'Compare modern UI frameworks'
    },
    { 
      title: 'Backend Services', 
      techs: ['Node.js', 'Django', 'Spring'],
      icon: <BarChart3 className="w-5 h-5" />,
      description: 'Evaluate server technologies'
    },
    { 
      title: 'Cloud Platforms', 
      techs: ['AWS', 'Azure', 'GCP'],
      icon: <Brain className="w-5 h-5" />,
      description: 'Choose your cloud provider'
    }
  ]

  return (
    <div className="relative min-h-screen flex items-center justify-center overflow-hidden">
      {/* Aurora Background Effect (Dark Mode) */}
      <div className="absolute inset-0 dark:block hidden">
        <div className="aurora-glow absolute top-1/4 left-1/2 w-96 h-96 -translate-x-1/2 -translate-y-1/2" />
        <div className="absolute inset-0 void-gradient" />
      </div>
      
      {/* Light Mode Gradient */}
      <div className="absolute inset-0 mesh-gradient dark:hidden" />
      
      {/* Floating Particles Effect */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        {[...Array(6)].map((_, i) => (
          <div
            key={i}
            className="absolute w-2 h-2 bg-purple-400/30 rounded-full animate-float"
            style={{
              left: `${20 + i * 15}%`,
              top: `${30 + (i % 3) * 20}%`,
              animationDelay: `${i * 0.5}s`,
              animationDuration: `${4 + i}s`
            }}
          />
        ))}
      </div>
      
      {/* Hero Content */}
      <div className="relative z-10 text-center px-4 sm:px-6 lg:px-8 max-w-5xl mx-auto">
        <h1 className="text-5xl md:text-7xl font-bold text-purple-900 dark:text-purple-400 mb-6 animate-float">
          Choose Your
          <span className="block text-transparent bg-clip-text bg-gradient-to-r from-purple-600 to-purple-800 dark:from-purple-400 dark:to-purple-600 neon-text">
            Tech Stack
          </span>
        </h1>
        
        <p className="text-xl md:text-2xl text-purple-700 dark:text-purple-300 mb-12 max-w-3xl mx-auto leading-relaxed">
          Compare technologies, analyze trade-offs, and make informed decisions with AI-powered insights
        </p>
        
        {/* Search Input */}
        <form onSubmit={handleSearch} className="relative max-w-2xl mx-auto mb-16 group">
          <div className="relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-6 h-6 text-purple-500 dark:text-purple-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search technologies to compare (e.g., React, Vue, Angular)"
              className="w-full pl-12 pr-16 py-5 text-lg rounded-2xl neon-border glassmorphism text-purple-900 dark:text-purple-100 placeholder-purple-500 dark:placeholder-purple-400 search-input focus:outline-none transition-all duration-300"
            />
            <button
              type="submit"
              className="absolute right-2 top-1/2 -translate-y-1/2 p-3 rounded-xl bg-purple-600 hover:bg-purple-700 dark:bg-purple-500 dark:hover:bg-purple-600 text-white transition-all duration-300 glow-on-hover"
            >
              <ArrowRight className="w-5 h-5" />
            </button>
          </div>
          <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-purple-500/20 to-purple-600/20 dark:from-purple-400/20 dark:to-purple-500/20 -z-10 blur-xl opacity-0 group-focus-within:opacity-100 transition-opacity duration-300" />
        </form>
        
        {/* Example Cards */}
        <div className="grid md:grid-cols-3 gap-6 max-w-5xl mx-auto">
          {exampleComparisons.map((example, index) => (
            <div
              key={index}
              className="comparison-card glassmorphism rounded-xl p-6 neon-border cursor-pointer group glow-on-hover"
              onClick={() => navigate(`/compare?q=${example.techs.join(',')}`)}
            >
              <div className="flex items-center gap-3 mb-4">
                <div className="p-2 rounded-lg bg-purple-100 dark:bg-purple-900/50 text-purple-600 dark:text-purple-400">
                  {example.icon}
                </div>
                <h3 className="text-lg font-semibold text-purple-900 dark:text-purple-300">
                  {example.title}
                </h3>
              </div>
              
              <p className="text-sm text-purple-600 dark:text-purple-400 mb-4">
                {example.description}
              </p>
              
              <div className="flex flex-wrap gap-2">
                {example.techs.map((tech) => (
                  <span
                    key={tech}
                    className="px-3 py-1 text-sm bg-purple-100 dark:bg-purple-900/50 text-purple-700 dark:text-purple-300 rounded-full border border-purple-200 dark:border-purple-700"
                  >
                    {tech}
                  </span>
                ))}
              </div>
              
              <div className="mt-4 flex items-center text-purple-600 dark:text-purple-400 text-sm font-medium group-hover:text-purple-700 dark:group-hover:text-purple-300 transition-colors">
                Compare now
                <ArrowRight className="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" />
              </div>
            </div>
          ))}
        </div>
        
        {/* Features Preview */}
        <div className="mt-20 grid md:grid-cols-3 gap-8 max-w-4xl mx-auto">
          <div className="text-center">
            <div className="w-12 h-12 mx-auto mb-4 rounded-full bg-purple-100 dark:bg-purple-900/50 flex items-center justify-center">
              <BarChart3 className="w-6 h-6 text-purple-600 dark:text-purple-400" />
            </div>
            <h3 className="text-lg font-semibold text-purple-900 dark:text-purple-300 mb-2">
              Data-Driven Analysis
            </h3>
            <p className="text-purple-600 dark:text-purple-400 text-sm">
              Compare technologies using real metrics and performance data
            </p>
          </div>
          
          <div className="text-center">
            <div className="w-12 h-12 mx-auto mb-4 rounded-full bg-purple-100 dark:bg-purple-900/50 flex items-center justify-center">
              <Brain className="w-6 h-6 text-purple-600 dark:text-purple-400" />
            </div>
            <h3 className="text-lg font-semibold text-purple-900 dark:text-purple-300 mb-2">
              AI-Powered Insights
            </h3>
            <p className="text-purple-600 dark:text-purple-400 text-sm">
              Get personalized recommendations based on your project needs
            </p>
          </div>
          
          <div className="text-center">
            <div className="w-12 h-12 mx-auto mb-4 rounded-full bg-purple-100 dark:bg-purple-900/50 flex items-center justify-center">
              <Zap className="w-6 h-6 text-purple-600 dark:text-purple-400" />
            </div>
            <h3 className="text-lg font-semibold text-purple-900 dark:text-purple-300 mb-2">
              Interactive Visualizations
            </h3>
            <p className="text-purple-600 dark:text-purple-400 text-sm">
              Explore comparisons through charts, graphs, and interactive elements
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
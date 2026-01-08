import React from 'react'

export default function LandingPage() {
  return (
    <div className="relative min-h-screen flex items-center justify-center">
      {/* Aurora Background Effect (Dark Mode) */}
      <div className="absolute inset-0 aurora-bg dark:block hidden" />
      
      {/* Light Mode Gradient */}
      <div className="absolute inset-0 bg-gradient-to-br from-purple-50 via-white to-purple-100 dark:hidden" />
      
      {/* Hero Content */}
      <div className="relative z-10 text-center px-4 sm:px-6 lg:px-8 max-w-4xl mx-auto">
        <h1 className="text-5xl md:text-7xl font-bold text-purple-900 dark:text-purple-400 mb-6 animate-float">
          Choose Your
          <span className="block text-transparent bg-clip-text bg-gradient-to-r from-purple-600 to-purple-800 dark:from-purple-400 dark:to-purple-600">
            Tech Stack
          </span>
        </h1>
        
        <p className="text-xl md:text-2xl text-purple-700 dark:text-purple-300 mb-12 max-w-2xl mx-auto">
          Compare technologies, analyze trade-offs, and make informed decisions with AI-powered insights
        </p>
        
        {/* Search Input */}
        <div className="relative max-w-2xl mx-auto mb-16">
          <input
            type="text"
            placeholder="Search technologies to compare (e.g., React, Vue, Angular)"
            className="w-full px-6 py-4 text-lg rounded-2xl border-2 border-purple-200 dark:border-purple-500/50 bg-white/70 dark:bg-white/10 backdrop-blur-md text-purple-900 dark:text-purple-100 placeholder-purple-500 dark:placeholder-purple-400 search-input focus:outline-none focus:border-purple-500 dark:focus:border-purple-400 transition-all duration-300"
          />
          <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-purple-500/20 to-purple-600/20 dark:from-purple-400/20 dark:to-purple-500/20 -z-10 blur-xl opacity-0 group-focus-within:opacity-100 transition-opacity duration-300" />
        </div>
        
        {/* Example Cards */}
        <div className="grid md:grid-cols-3 gap-6 max-w-4xl mx-auto">
          {[
            { title: 'Frontend Frameworks', techs: ['React', 'Vue', 'Angular'] },
            { title: 'Backend Services', techs: ['Node.js', 'Django', 'Spring'] },
            { title: 'Cloud Platforms', techs: ['AWS', 'Azure', 'GCP'] }
          ].map((example, index) => (
            <div
              key={index}
              className="comparison-card glassmorphism rounded-xl p-6 border border-purple-200/50 dark:border-purple-500/30 cursor-pointer group"
            >
              <h3 className="text-lg font-semibold text-purple-900 dark:text-purple-300 mb-3">
                {example.title}
              </h3>
              <div className="flex flex-wrap gap-2">
                {example.techs.map((tech) => (
                  <span
                    key={tech}
                    className="px-3 py-1 text-sm bg-purple-100 dark:bg-purple-900/50 text-purple-700 dark:text-purple-300 rounded-full"
                  >
                    {tech}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
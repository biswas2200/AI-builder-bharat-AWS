import React from 'react'
import { useTheme } from '../contexts/ThemeContext'
import { Sun, Moon } from 'lucide-react'

interface LayoutProps {
  children: React.ReactNode
}

export default function Layout({ children }: LayoutProps) {
  const { theme, toggleTheme } = useTheme()

  return (
    <div className="min-h-screen bg-purple-50 dark:bg-slate-950 transition-colors duration-300">
      {/* Navigation */}
      <nav className="sticky top-0 z-50 glassmorphism border-b border-purple-200/50 dark:border-purple-500/20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold neon-text">
                DevDecision
              </h1>
            </div>
            
            {/* Theme Toggle */}
            <button
              onClick={toggleTheme}
              className="p-3 rounded-xl glassmorphism neon-border glow-on-hover transition-all duration-300"
              aria-label="Toggle theme"
            >
              {theme === 'light' ? (
                <Moon className="w-5 h-5 text-purple-700 dark:text-purple-400" />
              ) : (
                <Sun className="w-5 h-5 text-purple-700 dark:text-purple-400" />
              )}
            </button>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="relative">
        {children}
      </main>
    </div>
  )
}
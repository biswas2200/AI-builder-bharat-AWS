import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { ThemeProvider } from './contexts/ThemeContext'
import { ComparisonProvider } from './contexts/ComparisonContext'
import { ErrorBoundary } from './components/ErrorBoundary'
import Layout from './components/Layout'
import LandingPage from './pages/LandingPage'
import ComparisonDashboard from './pages/ComparisonDashboard'

function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider>
        <ComparisonProvider>
          <Router>
            <Layout>
              <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/compare" element={<ComparisonDashboard />} />
              </Routes>
            </Layout>
          </Router>
        </ComparisonProvider>
      </ThemeProvider>
    </ErrorBoundary>
  )
}

export default App
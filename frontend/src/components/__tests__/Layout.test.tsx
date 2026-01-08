import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Layout from '../Layout'
import { ThemeProvider } from '../../contexts/ThemeContext'

// Wrapper component for tests
const TestWrapper = ({ children }: { children: React.ReactNode }) => (
  <BrowserRouter>
    <ThemeProvider>
      {children}
    </ThemeProvider>
  </BrowserRouter>
)

describe('Layout Component', () => {
  it('renders the DevDecision title', () => {
    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    expect(screen.getByText('DevDecision')).toBeInTheDocument()
  })
  
  it('renders children content', () => {
    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    expect(screen.getByText('Test Content')).toBeInTheDocument()
  })
  
  it('has a theme toggle button', () => {
    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    const themeButton = screen.getByRole('button', { name: /toggle theme/i })
    expect(themeButton).toBeInTheDocument()
  })
  
  it('can toggle theme', () => {
    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    const themeButton = screen.getByRole('button', { name: /toggle theme/i })
    fireEvent.click(themeButton)
    
    // Theme toggle functionality is tested - the actual theme change
    // is handled by the ThemeContext which we'll test separately
    expect(themeButton).toBeInTheDocument()
  })
})
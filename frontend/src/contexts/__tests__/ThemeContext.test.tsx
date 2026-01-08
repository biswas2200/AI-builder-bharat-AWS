import { render, screen, fireEvent } from '@testing-library/react'
import { ThemeProvider, useTheme } from '../ThemeContext'

// Test component to access theme context
const TestComponent = () => {
  const { theme, toggleTheme } = useTheme()
  return (
    <div>
      <span data-testid="current-theme">{theme}</span>
      <button onClick={toggleTheme} data-testid="toggle-button">
        Toggle Theme
      </button>
    </div>
  )
}

describe('ThemeContext', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear()
    // Reset document classes
    document.documentElement.className = ''
  })
  
  it('provides default light theme', () => {
    render(
      <ThemeProvider>
        <TestComponent />
      </ThemeProvider>
    )
    
    expect(screen.getByTestId('current-theme')).toHaveTextContent('light')
  })
  
  it('can toggle between light and dark themes', () => {
    render(
      <ThemeProvider>
        <TestComponent />
      </ThemeProvider>
    )
    
    const themeDisplay = screen.getByTestId('current-theme')
    const toggleButton = screen.getByTestId('toggle-button')
    
    // Initially light
    expect(themeDisplay).toHaveTextContent('light')
    
    // Toggle to dark
    fireEvent.click(toggleButton)
    expect(themeDisplay).toHaveTextContent('dark')
    
    // Toggle back to light
    fireEvent.click(toggleButton)
    expect(themeDisplay).toHaveTextContent('light')
  })
  
  it('applies theme class to document element', () => {
    render(
      <ThemeProvider>
        <TestComponent />
      </ThemeProvider>
    )
    
    expect(document.documentElement.classList.contains('light')).toBe(true)
    
    const toggleButton = screen.getByTestId('toggle-button')
    fireEvent.click(toggleButton)
    
    expect(document.documentElement.classList.contains('dark')).toBe(true)
    expect(document.documentElement.classList.contains('light')).toBe(false)
  })
})
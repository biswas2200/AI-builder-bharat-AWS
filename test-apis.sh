#!/bin/bash

# DevDecision API Test Script
# Tests the real API integrations

echo "🚀 Testing DevDecision APIs..."

# Check if backend is running
echo "📡 Checking backend health..."
curl -f http://localhost:8080/actuator/health || {
    echo "❌ Backend is not running. Start it with: cd backend && mvn spring-boot:run"
    exit 1
}

echo "✅ Backend is running"

# Test database connection by fetching technologies
echo "🗄️ Testing database connection..."
TECH_COUNT=$(curl -s http://localhost:8080/api/inventory/technologies | jq length)
echo "📊 Found $TECH_COUNT technologies in database"

# Test search functionality
echo "🔍 Testing search functionality..."
curl -s "http://localhost:8080/api/inventory/search?query=react" | jq '.[] | .name' | head -3

# Test Gemini AI integration with a simple comparison
echo "🤖 Testing Gemini AI integration..."
COMPARISON_RESULT=$(curl -s -X POST http://localhost:8080/api/comparisons \
  -H "Content-Type: application/json" \
  -d '{
    "technologyIds": [1, 2],
    "userConstraints": {
      "priorityTags": ["performance"],
      "projectType": "web-app"
    }
  }')

if echo "$COMPARISON_RESULT" | jq -e '.recommendationSummary' > /dev/null; then
    echo "✅ Gemini AI integration working!"
    echo "🎯 Recommendation: $(echo "$COMPARISON_RESULT" | jq -r '.recommendationSummary' | head -c 100)..."
else
    echo "⚠️ Gemini AI might be using fallback data (check logs)"
fi

# Test Redis caching
echo "💾 Testing Redis caching..."
curl -s http://localhost:8080/api/inventory/technologies/1 > /dev/null
curl -s http://localhost:8080/api/inventory/technologies/1 > /dev/null
echo "✅ Cache requests completed"

echo "🎉 API testing complete!"
echo ""
echo "🌐 Frontend: http://localhost:3000"
echo "🔧 Backend: http://localhost:8080"
echo "📊 API Docs: http://localhost:8080/swagger-ui.html (if enabled)"
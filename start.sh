#!/bin/bash

echo "🚀 Starting ContextChain..."
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    echo "⚙️  Creating .env file from template..."
    cp .env.example .env
    echo "⚠️  WARNING: Please edit .env and set secure passwords before production use!"
    echo ""
fi

# Start Docker services
echo "🐳 Starting PostgreSQL and Backend with Docker..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to be ready..."
echo ""

# Wait for backend health check
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if curl -s http://localhost:8080/health > /dev/null 2>&1; then
        echo "✅ Backend is ready!"
        break
    fi

    attempt=$((attempt + 1))
    echo "   Waiting for backend... (attempt $attempt/$max_attempts)"
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "❌ Backend failed to start. Check logs with: docker-compose logs backend"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ ContextChain Backend is running!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📊 Backend API:  http://localhost:8080"
echo "🏥 Health Check: http://localhost:8080/health"
echo "🗄️  PostgreSQL:  localhost:5432"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📝 Next Steps:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Start the frontend:"
echo "   cd frontend && npm install && npm run dev"
echo ""
echo "2. Open browser to: http://localhost:5173"
echo ""
echo "3. Configure your Anthropic API key in Settings"
echo ""
echo "4. Upload a sample transcript from: sample-transcripts/"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔧 Useful Commands:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "View logs:     docker-compose logs -f backend"
echo "Stop services: docker-compose down"
echo "Restart:       docker-compose restart backend"
echo ""

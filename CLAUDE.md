# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ContextChain** is a meeting intelligence application for engineering and architecture leaders. It provides AI-powered analysis of meeting transcripts, extracting summaries, action items, decisions, and insights using Anthropic's Claude API.

## Architecture

### Tech Stack
- **Backend**: Java 21, Spring Boot 3.x, PostgreSQL
- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS
- **AI**: Anthropic Claude API (user-provided API key)
- **Database**: PostgreSQL 16 with JSONB support

### Monorepo Structure
```
recall-context/
├── backend/          # Spring Boot application
├── frontend/         # React + Vite application
├── sample-transcripts/  # Example transcript files
└── docker-compose.yml   # Local development setup
```

## Common Development Commands

### Option A: Docker (Recommended for Quick Setup)

**Configure environment:**
```bash
cp .env.example .env
# Edit .env to set secure passwords
```

**Start PostgreSQL + Backend:**
```bash
docker-compose up -d
# Backend: http://localhost:8080
# PostgreSQL: localhost:5432
```

**Start Frontend:**
```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:5173
```

**View logs:**
```bash
docker-compose logs -f backend
```

**Stop services:**
```bash
docker-compose down
```

> See [DOCKER.md](DOCKER.md) for detailed Docker instructions and troubleshooting.

### Option B: Local Development (Faster Iteration)

**Start PostgreSQL only:**
```bash
docker-compose up -d postgres
```

**Backend (runs locally for hot-reload):**
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# Runs on http://localhost:8080
```

**Frontend (runs locally for HMR):**
```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:5173
```

### Building

**Backend:**
```bash
cd backend
mvn clean package
# Output: target/recall-context-backend-1.0.0.jar
```

**Frontend:**
```bash
cd frontend
npm run build
# Output: dist/
```

### Database

**Connect to PostgreSQL:**
```bash
psql -h localhost -U recall_user -d recall_context
# Password: recall_password
```

**Run migrations:**
Flyway migrations run automatically on startup. Manual migration:
```bash
cd backend
mvn flyway:migrate
```

## Key Implementation Details

### Transcript Filename Convention
Format: `YYYY-MM-DD_HHmm_MeetingType_SeriesName.txt`

**Valid Meeting Types:**
- Recurring: OneOnOne, Standup, Programme, Retro, Governance, Leadership
- Ad-hoc: Vendor, Adhoc, Incident, Interview, Review
- Personal: Dictation

**Example:** `2026-01-11_1400_OneOnOne_WeeklySync.txt`

### API Key Security
- User-provided Anthropic API keys are encrypted using AES-256
- Keys are encrypted with PBKDF2-derived keys (master secret + user ID)
- Stored in `user_settings` table with encryption IV
- Never exposed in API responses or logs

### AI Processing Flow
1. User uploads transcript → `MeetingController.uploadTranscript()`
2. Filename parsed → `TranscriptParserService.parseFilename()`
3. Meeting saved with status `PROCESSING`
4. AI analysis → `AnthropicService.analyzeMeetingTranscript()`
5. Results stored → `SummaryService.analyzeAndStoreMeeting()`
6. Status updated to `COMPLETED` or `FAILED`

### Database Schema Key Tables
- `meetings`: Core meeting data, transcript content, processing status
- `summaries`: AI-generated summaries (key points, decisions, sentiment)
- `action_items`: Extracted tasks with assignee, due date, status
- `participants`: Meeting attendees extracted from transcript
- `meeting_series`: Recurring meeting series grouping
- `user_settings`: Encrypted API keys

## Critical Files

### Backend Core Services
- `backend/src/main/java/com/recallcontext/service/MeetingService.java` - Orchestrates upload workflow
- `backend/src/main/java/com/recallcontext/service/AnthropicService.java` - Claude API integration
- `backend/src/main/java/com/recallcontext/service/EncryptionService.java` - API key encryption
- `backend/src/main/java/com/recallcontext/service/SummaryService.java` - Store AI analysis results

### Frontend Core Components
- `frontend/src/components/meetings/MeetingUpload.tsx` - File upload with drag-drop
- `frontend/src/components/meetings/MeetingDetail.tsx` - Full meeting view
- `frontend/src/components/settings/SettingsPage.tsx` - API key configuration
- `frontend/src/services/api.ts` - Axios HTTP client
- `frontend/src/services/meetingService.ts` - Meeting API calls

### Configuration
- `backend/src/main/resources/application-dev.yml` - Development config
- `backend/src/main/resources/prompts/meeting-analysis-prompt.txt` - AI prompt template
- `frontend/vite.config.ts` - Vite dev server with proxy

## API Endpoints

**Settings:**
- `POST /api/v1/settings/api-key` - Save API key
- `GET /api/v1/settings/api-key/status` - Check if configured
- `DELETE /api/v1/settings/api-key` - Delete API key

**Meetings:**
- `POST /api/v1/meetings` - Upload transcript (processes with AI)
- `GET /api/v1/meetings` - List meetings (paginated)
- `GET /api/v1/meetings/{id}` - Get meeting details
- `GET /api/v1/meetings/{id}/processing-status` - Poll status
- `DELETE /api/v1/meetings/{id}` - Delete meeting

**Actions:**
- `GET /api/v1/actions` - List all actions (paginated)
- `PUT /api/v1/actions/{id}` - Update action
- `PATCH /api/v1/actions/{id}/status` - Update status only

**Health:**
- `GET /health` - Health check for Cloud Run

## Environment Variables

### Backend (dev)
- `SPRING_PROFILES_ACTIVE=dev`
- `ENCRYPTION_SECRET` - Master encryption secret (default in dev profile)

### Backend (prod)
- `DB_URL` - PostgreSQL connection URL
- `DB_USERNAME`, `DB_PASSWORD` - Database credentials
- `ENCRYPTION_SECRET` - Master encryption secret (REQUIRED)

### Frontend
- `VITE_API_URL` - Backend API URL (default: http://localhost:8080)

## Testing the Application

1. **Start services:**
   ```bash
   docker-compose up -d postgres
   cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev &
   cd frontend && npm run dev
   ```

2. **Configure API key:**
   - Navigate to http://localhost:5173/settings
   - Enter your Anthropic API key
   - Click "Save API Key"

3. **Upload sample transcript:**
   - Go to http://localhost:5173/upload
   - Upload `sample-transcripts/2026-01-11_1400_OneOnOne_WeeklySync.txt`
   - AI processing happens immediately
   - View results on detail page

## Common Issues

**"API key not configured" error:**
- Go to Settings page and configure your Anthropic API key

**Database connection failed:**
- Ensure PostgreSQL is running: `docker-compose up -d postgres`
- Check connection: `psql -h localhost -U recall_user -d recall_context`

**Frontend can't reach backend:**
- Verify backend is running on port 8080
- Check Vite proxy configuration in `vite.config.ts`

**AI processing fails:**
- Check API key is valid
- Check backend logs for Anthropic API errors
- Verify transcript content is valid text

## Future Enhancements (Not Yet Implemented)

The following features were planned but not implemented in the MVP:
- Weekly/monthly roll-up reports
- Historical linkage for meeting series
- Full-text search across transcripts
- Analytics dashboard
- Meeting prep mode
- Export functionality (PDF)
- Multi-user authentication
- Email notifications
- Advanced filters

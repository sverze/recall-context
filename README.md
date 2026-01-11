# ContextChain - Meeting Intelligence Application

An AI-powered meeting intelligence application for engineering and architecture leaders. Upload meeting transcripts and get instant AI analysis with summaries, action items, decisions, and insights powered by Anthropic's Claude.

## Features

✅ **Smart Transcript Processing**
- Upload meeting transcripts with structured filename convention
- Automatic metadata extraction (date, time, type, series)
- Support for recurring and ad-hoc meetings

✅ **AI-Powered Analysis** (via Anthropic Claude)
- Automatic summary generation
- Key points and decisions extraction
- Action item identification with assignees and due dates
- Sentiment and tone analysis
- Participant extraction

✅ **Action Tracking**
- Track action items across all meetings
- Update status (Not Started, In Progress, Completed, Blocked)
- Filter by assignee, due date, or meeting
- Priority levels (high, medium, low)

✅ **Secure API Key Management**
- User-provided Anthropic API keys
- AES-256 encryption at rest
- Keys never exposed in API responses

## Quick Start

### Prerequisites
- Docker & Docker Compose (recommended) OR
- Java 21+ and PostgreSQL 16+ for manual setup
- Node.js 20+ (for frontend)
- Anthropic API key ([Get one here](https://console.anthropic.com/))

### Option A: Docker (Recommended)

**1. Configure environment:**
```bash
cp .env.example .env
# Edit .env and set POSTGRES_PASSWORD and ENCRYPTION_SECRET
```

**2. Start database and backend:**
```bash
docker-compose up -d
```

Backend runs on http://localhost:8080

**3. Start frontend:**
```bash
cd frontend
npm install
npm run dev
```

Frontend runs on http://localhost:5173

> For detailed Docker instructions, see [DOCKER.md](DOCKER.md)

### Option B: Manual Setup

**1. Start PostgreSQL:**
```bash
docker-compose up -d postgres
# OR install PostgreSQL locally
```

**2. Start Backend:**
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
Backend runs on http://localhost:8080

**3. Start Frontend:**
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on http://localhost:5173

### 4. Configure API Key
1. Navigate to http://localhost:5173/settings
2. Enter your Anthropic API key
3. Click "Save API Key"

### 5. Test with Sample Transcript
1. Go to http://localhost:5173/upload
2. Upload `sample-transcripts/2026-01-11_1400_OneOnOne_WeeklySync.txt`
3. View the AI-generated analysis!

## Architecture

### Tech Stack
- **Backend**: Java 21, Spring Boot 3.x, PostgreSQL
- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS
- **AI**: Anthropic Claude API (Claude 3.5 Sonnet)
- **Database**: PostgreSQL 16 with JSONB support

### Key Components

**Backend Services:**
- `MeetingService` - Orchestrates transcript upload and processing
- `AnthropicService` - Claude API integration for AI analysis
- `EncryptionService` - Secure API key storage
- `TranscriptParserService` - Filename parsing and validation

**Frontend Components:**
- `MeetingUpload` - Drag-and-drop transcript upload
- `MeetingDetail` - Full meeting view with summary and actions
- `MeetingList` - Browse all meetings
- `SettingsPage` - API key configuration

## Transcript Filename Convention

Format: `YYYY-MM-DD_HHmm_MeetingType_SeriesName.txt`

**Valid Meeting Types:**
- **Recurring**: OneOnOne, Standup, Programme, Retro, Governance, Leadership
- **Ad-hoc**: Vendor, Adhoc, Incident, Interview, Review
- **Personal**: Dictation

**Example:** `2026-01-11_1400_OneOnOne_WeeklySync.txt`

## API Endpoints

### Settings
- `POST /api/v1/settings/api-key` - Save encrypted API key
- `GET /api/v1/settings/api-key/status` - Check configuration status
- `DELETE /api/v1/settings/api-key` - Delete API key

### Meetings
- `POST /api/v1/meetings` - Upload and process transcript
- `GET /api/v1/meetings` - List all meetings (paginated)
- `GET /api/v1/meetings/{id}` - Get meeting details
- `DELETE /api/v1/meetings/{id}` - Delete meeting

### Actions
- `GET /api/v1/actions` - List all action items
- `PUT /api/v1/actions/{id}` - Update action item
- `PATCH /api/v1/actions/{id}/status` - Update status only

## Database Schema

Key tables:
- `meetings` - Core meeting data and transcripts
- `summaries` - AI-generated summaries
- `action_items` - Extracted tasks with status tracking
- `participants` - Meeting attendees
- `meeting_series` - Recurring meeting groupings
- `user_settings` - Encrypted API keys

## Development

### Build Backend
```bash
cd backend
mvn clean package
```

### Build Frontend
```bash
cd frontend
npm run build
```

### Run Tests
```bash
cd backend
mvn test
```

### Database Migrations
Flyway migrations run automatically on startup. Schema is in:
`backend/src/main/resources/db/migration/V1__initial_schema.sql`

## Deployment

### Backend (Cloud Run)
```bash
cd backend
mvn clean package -DskipTests
docker build -t gcr.io/PROJECT_ID/recall-context-backend .
docker push gcr.io/PROJECT_ID/recall-context-backend

gcloud run deploy recall-context-backend \
  --image gcr.io/PROJECT_ID/recall-context-backend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-secrets ENCRYPTION_SECRET=encryption-key:latest \
  --timeout 300 \
  --memory 1Gi
```

### Frontend (Firebase Hosting)
```bash
cd frontend
npm run build
firebase deploy --only hosting
```

## Environment Variables

### Backend
- `SPRING_PROFILES_ACTIVE` - Profile (dev/prod)
- `DB_URL` - PostgreSQL connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `ENCRYPTION_SECRET` - Master encryption secret (REQUIRED in production)

### Frontend
- `VITE_API_URL` - Backend API URL

## Security Features

- **API Key Encryption**: AES-256 encryption with PBKDF2 key derivation
- **SQL Injection Prevention**: JPA parameterized queries
- **CORS Configuration**: Whitelist-based origin control
- **Input Validation**: Bean validation on all API inputs
- **Error Handling**: Sanitized error messages, no stack traces exposed

## Future Enhancements

The following features are planned but not yet implemented:
- Weekly/monthly roll-up reports
- Historical linkage for meeting series
- Full-text search across transcripts
- Analytics dashboard with charts
- Meeting prep mode (context from previous meetings)
- Export functionality (PDF, formatted text)
- Multi-user authentication and authorization
- Email notifications for overdue actions
- Advanced filtering and smart search

## Project Structure

```
recall-context/
├── backend/
│   ├── src/main/java/com/recallcontext/
│   │   ├── controller/       # REST controllers
│   │   ├── service/          # Business logic
│   │   ├── model/           # Entities and DTOs
│   │   ├── repository/      # JPA repositories
│   │   ├── exception/       # Custom exceptions
│   │   └── config/          # Spring configuration
│   ├── src/main/resources/
│   │   ├── db/migration/    # Flyway SQL migrations
│   │   ├── prompts/         # AI prompt templates
│   │   └── application.yml  # Configuration
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/      # React components
│   │   ├── services/        # API services
│   │   ├── types/          # TypeScript types
│   │   └── hooks/          # Custom React hooks
│   ├── package.json
│   └── vite.config.ts
├── sample-transcripts/      # Example files
├── docker-compose.yml       # Local development
└── CLAUDE.md               # Development guide

```

## License

This project is private and proprietary.

## Support

For issues or questions, please refer to the CLAUDE.md file for detailed development guidance.

# Docker Setup Guide

This guide explains how to run ContextChain using Docker Compose.

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+

## Quick Start

### 1. Configure Environment

Copy the example environment file and customize if needed:

```bash
cp .env.example .env
```

Edit `.env` to set your values:
```bash
# Database Configuration
POSTGRES_DB=recall_context
POSTGRES_USER=recall_user
POSTGRES_PASSWORD=your_secure_password

# Backend Configuration
SPRING_PROFILES_ACTIVE=dev
ENCRYPTION_SECRET=your-secure-random-string-min-32-chars

# Ports
POSTGRES_PORT=5432
BACKEND_PORT=8080
FRONTEND_PORT=5173
```

**Important:** Change `POSTGRES_PASSWORD` and `ENCRYPTION_SECRET` to secure values in production!

### 2. Start Services

Start both PostgreSQL and backend:

```bash
docker-compose up -d
```

This will:
- Start PostgreSQL on port 5432
- Build and start the backend on port 8080
- Create a Docker network for service communication
- Set up persistent volumes for database data

### 3. Check Status

```bash
# View running containers
docker-compose ps

# View logs
docker-compose logs -f backend

# View database logs
docker-compose logs -f postgres
```

### 4. Wait for Backend to be Ready

The backend will:
1. Wait for PostgreSQL to be healthy
2. Run Flyway database migrations
3. Start the Spring Boot application
4. Be available at http://localhost:8080

Check health:
```bash
curl http://localhost:8080/health
```

Expected response:
```json
{
  "status": "UP",
  "timestamp": "2026-01-11T...",
  "service": "recall-context-backend"
}
```

## Frontend Setup

The frontend runs separately (not in Docker by default for faster development):

```bash
cd frontend
npm install
npm run dev
```

Frontend will be available at http://localhost:5173

## Common Commands

### Start services
```bash
docker-compose up -d
```

### Stop services
```bash
docker-compose down
```

### Stop services and remove volumes (WARNING: deletes database data)
```bash
docker-compose down -v
```

### Restart backend only
```bash
docker-compose restart backend
```

### Rebuild backend after code changes
```bash
docker-compose up -d --build backend
```

### View logs
```bash
# All services
docker-compose logs -f

# Backend only
docker-compose logs -f backend

# Postgres only
docker-compose logs -f postgres

# Last 100 lines
docker-compose logs --tail=100 backend
```

### Connect to PostgreSQL
```bash
# From host (if psql is installed)
psql -h localhost -p 5432 -U recall_user -d recall_context

# From within container
docker-compose exec postgres psql -U recall_user -d recall_context
```

### Execute commands in backend container
```bash
docker-compose exec backend sh
```

## Architecture

### Services

**postgres** (PostgreSQL 16)
- Database for persistent storage
- Health check: `pg_isready`
- Restart policy: unless-stopped
- Volume: `postgres_data` for persistence

**backend** (Spring Boot)
- Java 21 application
- Multi-stage build for optimization
- Runs as non-root user
- Health check: `/health` endpoint
- Restart policy: unless-stopped
- Waits for PostgreSQL health check

### Network

Both services communicate via `recall-network` (bridge network). This allows:
- Backend to connect to postgres via hostname `postgres`
- Isolated networking from host
- Service discovery by container name

### Volumes

- `postgres_data`: Persistent database storage

## Environment Variables

### PostgreSQL
- `POSTGRES_DB`: Database name
- `POSTGRES_USER`: Database username
- `POSTGRES_PASSWORD`: Database password

### Backend
- `SPRING_PROFILES_ACTIVE`: Spring profile (dev/prod)
- `SPRING_DATASOURCE_URL`: JDBC connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `ENCRYPTION_SECRET`: Master secret for API key encryption

## Troubleshooting

### Backend won't start

**Check logs:**
```bash
docker-compose logs backend
```

**Common issues:**
1. PostgreSQL not healthy yet - wait 30 seconds and check again
2. Database connection refused - check PostgreSQL logs
3. Port 8080 already in use - change `BACKEND_PORT` in `.env`

### Database connection errors

**Check PostgreSQL is running:**
```bash
docker-compose ps postgres
```

**Check PostgreSQL logs:**
```bash
docker-compose logs postgres
```

**Test connection:**
```bash
docker-compose exec postgres pg_isready -U recall_user
```

### Backend health check failing

**Check if application is running:**
```bash
docker-compose exec backend wget -O- http://localhost:8080/health
```

**If not responding:**
1. Check backend logs for startup errors
2. Verify database migrations completed successfully
3. Check memory/CPU constraints

### Port conflicts

If ports are already in use, change them in `.env`:
```bash
POSTGRES_PORT=5433
BACKEND_PORT=8081
```

Then restart:
```bash
docker-compose down
docker-compose up -d
```

### Clean slate restart

To completely reset (WARNING: deletes all data):
```bash
# Stop and remove everything
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Start fresh
docker-compose up -d --build
```

## Production Deployment

For production:

1. **Update `.env` with secure values:**
   ```bash
   POSTGRES_PASSWORD=<strong-random-password>
   ENCRYPTION_SECRET=<cryptographically-secure-random-string>
   SPRING_PROFILES_ACTIVE=prod
   ```

2. **Use secrets management:**
   - Don't commit `.env` to version control
   - Use Docker secrets or external secret manager
   - Rotate secrets regularly

3. **Update compose for production:**
   ```yaml
   # docker-compose.prod.yml
   services:
     backend:
       restart: always
       deploy:
         resources:
           limits:
             memory: 1G
           reservations:
             memory: 512M
   ```

4. **Enable SSL/TLS:**
   - Use reverse proxy (nginx, Traefik)
   - Configure SSL certificates
   - Update CORS settings

5. **Backup strategy:**
   ```bash
   # Backup database
   docker-compose exec postgres pg_dump -U recall_user recall_context > backup.sql

   # Restore database
   docker-compose exec -T postgres psql -U recall_user recall_context < backup.sql
   ```

## Performance Tuning

### Backend JVM Options

Edit `backend/Dockerfile`:
```dockerfile
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"
```

### PostgreSQL Tuning

Create `postgres-config/postgresql.conf` and mount it:
```yaml
postgres:
  volumes:
    - ./postgres-config/postgresql.conf:/etc/postgresql/postgresql.conf
  command: postgres -c config_file=/etc/postgresql/postgresql.conf
```

## Monitoring

### Check container resource usage
```bash
docker stats
```

### Check container health
```bash
docker-compose ps
```

### Export logs
```bash
docker-compose logs --no-color > logs.txt
```

## Development Workflow

For local development with hot-reload:

1. **Keep database in Docker:**
   ```bash
   docker-compose up -d postgres
   ```

2. **Run backend locally:**
   ```bash
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Run frontend locally:**
   ```bash
   cd frontend
   npm run dev
   ```

This gives you:
- Fast backend recompilation
- Hot module replacement in frontend
- Persistent database in Docker
- Best development experience

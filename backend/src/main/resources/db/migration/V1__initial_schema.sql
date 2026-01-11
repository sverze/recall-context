-- User Settings (for API key storage)
CREATE TABLE user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE NOT NULL DEFAULT 'default-user',
    encrypted_api_key TEXT NOT NULL,
    encryption_iv TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Meeting Series (for recurring meetings)
CREATE TABLE meeting_series (
    id BIGSERIAL PRIMARY KEY,
    series_name VARCHAR(255) NOT NULL,
    meeting_type VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(series_name, meeting_type)
);

CREATE INDEX idx_series_name ON meeting_series(series_name);
CREATE INDEX idx_series_type ON meeting_series(meeting_type);

-- Meetings
CREATE TABLE meetings (
    id BIGSERIAL PRIMARY KEY,
    series_id BIGINT REFERENCES meeting_series(id) ON DELETE SET NULL,
    meeting_date TIMESTAMP NOT NULL,
    meeting_type VARCHAR(50) NOT NULL,
    series_name VARCHAR(255),
    original_filename VARCHAR(500) NOT NULL,
    transcript_content TEXT NOT NULL,
    metadata JSONB,
    processing_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    processing_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meeting_date ON meetings(meeting_date DESC);
CREATE INDEX idx_meeting_type ON meetings(meeting_type);
CREATE INDEX idx_series_id ON meetings(series_id);
CREATE INDEX idx_processing_status ON meetings(processing_status);
CREATE INDEX idx_created_at ON meetings(created_at DESC);

-- Participants
CREATE TABLE participants (
    id BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_participant_meeting_id ON participants(meeting_id);
CREATE INDEX idx_participant_name ON participants(name);

-- Summaries
CREATE TABLE summaries (
    id BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT UNIQUE NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    key_points TEXT[] NOT NULL,
    decisions TEXT[] NOT NULL,
    summary_text TEXT NOT NULL,
    sentiment VARCHAR(50),
    tone VARCHAR(50),
    ai_metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_summary_meeting_id ON summaries(meeting_id);

-- Action Items
CREATE TABLE action_items (
    id BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    assignee VARCHAR(255),
    due_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    priority VARCHAR(20),
    notes TEXT,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_action_meeting_id ON action_items(meeting_id);
CREATE INDEX idx_action_assignee ON action_items(assignee);
CREATE INDEX idx_action_status ON action_items(status);
CREATE INDEX idx_action_due_date ON action_items(due_date);

-- Processing logs (for debugging and monitoring)
CREATE TABLE processing_logs (
    id BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT REFERENCES meetings(id) ON DELETE CASCADE,
    operation VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    details JSONB,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_log_meeting_id ON processing_logs(meeting_id);
CREATE INDEX idx_log_created_at ON processing_logs(created_at DESC);

-- Insert default user settings placeholder (will be updated when user provides API key)
INSERT INTO user_settings (user_id, encrypted_api_key, encryption_iv)
VALUES ('default-user', 'not-configured', 'not-configured')
ON CONFLICT (user_id) DO NOTHING;

CREATE TABLE IF NOT EXISTS pins (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    board_id UUID,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_key VARCHAR(1024) NOT NULL,
    source_url VARCHAR(512),
    tags TEXT[],
    visibility VARCHAR(32) NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_pins_user_id ON pins(user_id);
CREATE INDEX IF NOT EXISTS idx_pins_board_id ON pins(board_id);
CREATE INDEX IF NOT EXISTS idx_pins_created_at ON pins(created_at);
CREATE INDEX IF NOT EXISTS idx_pins_is_active ON pins(is_active);

-- Indexes and Unique Constraints

-- Users: unique phone number (E.164 format)
CREATE UNIQUE INDEX idx_users_phone_e164 ON users(phone_e164);

-- Users: optional email index for future email queries
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;

-- Posts: filter by deleted_at (for active posts)
CREATE INDEX idx_posts_deleted_at ON posts(deleted_at) WHERE deleted_at IS NULL;

-- Posts: sort by created_at descending (for list endpoint)
CREATE INDEX idx_posts_created_at_desc ON posts(created_at DESC);

-- Posts: author lookup
CREATE INDEX idx_posts_author_user_id ON posts(author_user_id);

-- Comments: filter by post_id and sort by created_at
CREATE INDEX idx_comments_post_id_created_at ON comments(post_id, created_at);

-- Comments: author lookup
CREATE INDEX idx_comments_author_user_id ON comments(author_user_id);

-- Refresh tokens: unique token_hash to prevent duplicates
CREATE UNIQUE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);

-- Refresh tokens: lookup by user_id
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- Refresh tokens: filter by expiration and revocation
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked_at ON refresh_tokens(revoked_at) WHERE revoked_at IS NULL;

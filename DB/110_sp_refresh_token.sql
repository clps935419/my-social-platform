-- Refresh Token Management Stored Procedures (with Rotation)
-- All timestamps in UTC (timestamptz)

-- SP: Issue new refresh token after login
-- Input: user_id, token_hash, expires_at
-- Output: refresh_token_id, issued_at
CREATE OR REPLACE FUNCTION sp_refresh_token_issue(
    p_user_id UUID,
    p_token_hash VARCHAR(255),
    p_expires_at TIMESTAMPTZ
)
RETURNS TABLE(
    refresh_token_id UUID,
    issued_at TIMESTAMPTZ
) AS $$
DECLARE
    v_token_id UUID;
    v_issued_at TIMESTAMPTZ;
BEGIN
    v_issued_at := NOW() AT TIME ZONE 'UTC';
    
    INSERT INTO refresh_tokens (user_id, token_hash, issued_at, expires_at)
    VALUES (p_user_id, p_token_hash, v_issued_at, p_expires_at)
    RETURNING refresh_tokens.refresh_token_id
    INTO v_token_id;

    RETURN QUERY
    SELECT v_token_id, v_issued_at;
END;
$$ LANGUAGE plpgsql;

-- SP: Validate refresh token
-- Input: token_hash
-- Output: user_id, refresh_token_id, expires_at (if valid)
-- Returns empty if token is invalid, revoked, or expired
CREATE OR REPLACE FUNCTION sp_refresh_token_validate(
    p_token_hash VARCHAR(255)
)
RETURNS TABLE(
    user_id UUID,
    refresh_token_id UUID,
    expires_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        rt.user_id,
        rt.refresh_token_id,
        rt.expires_at
    FROM refresh_tokens rt
    WHERE rt.token_hash = p_token_hash
      AND rt.revoked_at IS NULL
      AND rt.expires_at > (NOW() AT TIME ZONE 'UTC');
END;
$$ LANGUAGE plpgsql;

-- SP: Rotate refresh token (atomic: revoke old + issue new)
-- Input: old_token_hash, new_token_hash, new_expires_at
-- Output: new refresh_token_id, issued_at
-- Error: raises exception if old token is invalid
CREATE OR REPLACE FUNCTION sp_refresh_token_rotate(
    p_old_token_hash VARCHAR(255),
    p_new_token_hash VARCHAR(255),
    p_new_expires_at TIMESTAMPTZ
)
RETURNS TABLE(
    user_id UUID,
    refresh_token_id UUID,
    issued_at TIMESTAMPTZ
) AS $$
DECLARE
    v_user_id UUID;
    v_old_token_id UUID;
    v_new_token_id UUID;
    v_issued_at TIMESTAMPTZ;
BEGIN
    v_issued_at := NOW() AT TIME ZONE 'UTC';
    
    -- Validate and get old token info
    SELECT rt.user_id, rt.refresh_token_id
    INTO v_user_id, v_old_token_id
    FROM refresh_tokens rt
    WHERE rt.token_hash = p_old_token_hash
      AND rt.revoked_at IS NULL
      AND rt.expires_at > v_issued_at;

    -- If old token not found or invalid, raise error
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'INVALID_REFRESH_TOKEN' USING ERRCODE = '28000';
    END IF;

    -- Revoke old token
    UPDATE refresh_tokens
    SET revoked_at = v_issued_at,
        replaced_by_token_id = v_new_token_id
    WHERE refresh_token_id = v_old_token_id;

    -- Issue new token
    INSERT INTO refresh_tokens (user_id, token_hash, issued_at, expires_at)
    VALUES (v_user_id, p_new_token_hash, v_issued_at, p_new_expires_at)
    RETURNING refresh_tokens.refresh_token_id
    INTO v_new_token_id;

    -- Update the replaced_by_token_id reference
    UPDATE refresh_tokens
    SET replaced_by_token_id = v_new_token_id
    WHERE refresh_token_id = v_old_token_id;

    RETURN QUERY
    SELECT v_user_id, v_new_token_id, v_issued_at;
END;
$$ LANGUAGE plpgsql;

-- SP: Revoke refresh token (for logout or security)
-- Input: token_hash
-- Output: success boolean
CREATE OR REPLACE FUNCTION sp_refresh_token_revoke(
    p_token_hash VARCHAR(255)
)
RETURNS TABLE(
    success BOOLEAN
) AS $$
DECLARE
    v_revoked_at TIMESTAMPTZ;
BEGIN
    v_revoked_at := NOW() AT TIME ZONE 'UTC';
    
    UPDATE refresh_tokens
    SET revoked_at = v_revoked_at
    WHERE token_hash = p_token_hash
      AND revoked_at IS NULL;

    RETURN QUERY
    SELECT FOUND;
END;
$$ LANGUAGE plpgsql;

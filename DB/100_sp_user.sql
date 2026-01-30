-- User Management Stored Procedures
-- All timestamps in UTC (timestamptz)

-- SP: Register new user
-- Input: phone_e164, user_name, email, password_hash, password_salt, cover_image_url, biography
-- Output: user record
-- Error: 409 if phone_e164 already exists
CREATE OR REPLACE FUNCTION sp_user_register(
    p_phone_e164 VARCHAR(20),
    p_user_name VARCHAR(100),
    p_email VARCHAR(255),
    p_password_hash VARCHAR(255),
    p_password_salt VARCHAR(255),
    p_cover_image_url VARCHAR(2048),
    p_biography TEXT
)
RETURNS TABLE(
    user_id UUID,
    phone_e164 VARCHAR(20),
    user_name VARCHAR(100),
    email VARCHAR(255),
    cover_image_url VARCHAR(2048),
    biography TEXT,
    created_at TIMESTAMPTZ
) AS $$
DECLARE
    v_user_id UUID;
    v_created_at TIMESTAMPTZ;
BEGIN
    -- Check if phone number already exists
    IF EXISTS (SELECT 1 FROM users WHERE users.phone_e164 = p_phone_e164) THEN
        RAISE EXCEPTION 'PHONE_NUMBER_EXISTS' USING ERRCODE = '23505';
    END IF;

    -- Insert new user
    INSERT INTO users (phone_e164, user_name, email, password_hash, password_salt, cover_image_url, biography, created_at)
    VALUES (p_phone_e164, p_user_name, p_email, p_password_hash, p_password_salt, p_cover_image_url, p_biography, NOW() AT TIME ZONE 'UTC')
    RETURNING users.user_id, users.created_at
    INTO v_user_id, v_created_at;

    -- Return user profile
    RETURN QUERY
    SELECT v_user_id, p_phone_e164, p_user_name, p_email, p_cover_image_url, p_biography, v_created_at;
END;
$$ LANGUAGE plpgsql;

-- SP: Get user by phone for login
-- Input: phone_e164
-- Output: user_id, password_hash, password_salt, user profile
CREATE OR REPLACE FUNCTION sp_user_get_by_phone(
    p_phone_e164 VARCHAR(20)
)
RETURNS TABLE(
    user_id UUID,
    phone_e164 VARCHAR(20),
    user_name VARCHAR(100),
    email VARCHAR(255),
    password_hash VARCHAR(255),
    password_salt VARCHAR(255),
    cover_image_url VARCHAR(2048),
    biography TEXT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.user_id,
        u.phone_e164,
        u.user_name,
        u.email,
        u.password_hash,
        u.password_salt,
        u.cover_image_url,
        u.biography,
        u.created_at,
        u.updated_at
    FROM users u
    WHERE u.phone_e164 = p_phone_e164;
END;
$$ LANGUAGE plpgsql;

-- SP: Get user profile by user_id (for /me endpoint)
-- Input: user_id
-- Output: user profile (without password fields)
CREATE OR REPLACE FUNCTION sp_user_get_profile(
    p_user_id UUID
)
RETURNS TABLE(
    user_id UUID,
    phone_e164 VARCHAR(20),
    user_name VARCHAR(100),
    email VARCHAR(255),
    cover_image_url VARCHAR(2048),
    biography TEXT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.user_id,
        u.phone_e164,
        u.user_name,
        u.email,
        u.cover_image_url,
        u.biography,
        u.created_at,
        u.updated_at
    FROM users u
    WHERE u.user_id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- Post Stored Procedures

-- sp_post_list: List posts (newest first, exclude soft-deleted)
-- Returns: posts array with author info, total count
-- Parameters: p_limit (default 20), p_offset (default 0), p_author_user_id (optional filter)
CREATE OR REPLACE FUNCTION sp_post_list(
    p_limit INTEGER DEFAULT 20,
    p_offset INTEGER DEFAULT 0,
    p_author_user_id UUID DEFAULT NULL
)
RETURNS TABLE(
    post_id UUID,
    author_user_id UUID,
    author_user_name VARCHAR(100),
    author_cover_image_url VARCHAR(2048),
    content TEXT,
    image_url VARCHAR(2048),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    total_count BIGINT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_total_count BIGINT;
BEGIN
    -- Get total count of non-deleted posts (with optional author filter)
    SELECT COUNT(*) INTO v_total_count
    FROM posts
    WHERE deleted_at IS NULL
      AND (p_author_user_id IS NULL OR posts.author_user_id = p_author_user_id);
    
    -- Return posts with author info
    RETURN QUERY
    SELECT 
        p.post_id,
        p.author_user_id,
        u.user_name,
        u.cover_image_url,
        p.content,
        p.image_url,
        p.created_at,
        p.updated_at,
        v_total_count
    FROM posts p
    INNER JOIN users u ON p.author_user_id = u.user_id
    WHERE p.deleted_at IS NULL
      AND (p_author_user_id IS NULL OR p.author_user_id = p_author_user_id)
    ORDER BY p.created_at DESC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$;

-- sp_post_create: Create a new post
-- Parameters: p_author_user_id (uuid), p_content (text), p_image_url (varchar, nullable)
-- Returns: Post with author info
CREATE OR REPLACE FUNCTION sp_post_create(
    p_author_user_id UUID,
    p_content TEXT,
    p_image_url VARCHAR(2048) DEFAULT NULL
)
RETURNS TABLE(
    post_id UUID,
    author_user_id UUID,
    author_user_name VARCHAR(100),
    author_cover_image_url VARCHAR(2048),
    content TEXT,
    image_url VARCHAR(2048),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_post_id UUID;
    v_trimmed_content TEXT;
BEGIN
    -- Validate content (required and not all whitespace)
    v_trimmed_content := TRIM(p_content);
    IF v_trimmed_content = '' OR v_trimmed_content IS NULL THEN
        RAISE EXCEPTION 'Content is required and cannot be empty';
    END IF;
    
    -- Validate image URL if provided
    IF p_image_url IS NOT NULL THEN
        IF LENGTH(p_image_url) > 2048 THEN
            RAISE EXCEPTION 'Image URL exceeds maximum length of 2048 characters';
        END IF;
        IF p_image_url !~ '^https?://' THEN
            RAISE EXCEPTION 'Image URL must start with http:// or https://';
        END IF;
    END IF;
    
    -- Insert new post
    INSERT INTO posts (author_user_id, content, image_url, created_at, updated_at)
    VALUES (p_author_user_id, p_content, p_image_url, now(), now())
    RETURNING posts.post_id INTO v_post_id;
    
    -- Return post with author info
    RETURN QUERY
    SELECT 
        p.post_id,
        p.author_user_id,
        u.user_name,
        u.cover_image_url,
        p.content,
        p.image_url,
        p.created_at,
        p.updated_at
    FROM posts p
    INNER JOIN users u ON p.author_user_id = u.user_id
    WHERE p.post_id = v_post_id;
END;
$$;

-- sp_post_update: Update a post (author only)
-- Parameters: p_actor_user_id (uuid), p_post_id (uuid), p_content (text, nullable), p_image_url (varchar, nullable)
-- Returns: Post with author info + metadata (post_exists, post_deleted, is_author)
CREATE OR REPLACE FUNCTION sp_post_update(
    p_actor_user_id UUID,
    p_post_id UUID,
    p_content TEXT DEFAULT NULL,
    p_image_url VARCHAR(2048) DEFAULT NULL
)
RETURNS TABLE(
    post_id UUID,
    author_user_id UUID,
    author_user_name VARCHAR(100),
    author_cover_image_url VARCHAR(2048),
    content TEXT,
    image_url VARCHAR(2048),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    post_exists BOOLEAN,
    post_deleted BOOLEAN,
    is_author BOOLEAN
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_post_exists BOOLEAN;
    v_post_deleted BOOLEAN;
    v_is_author BOOLEAN;
    v_current_content TEXT;
    v_current_image_url VARCHAR(2048);
    v_new_content TEXT;
    v_trimmed_content TEXT;
BEGIN
    -- Check if post exists and get current values
    SELECT 
        EXISTS(SELECT 1 FROM posts WHERE posts.post_id = p_post_id),
        COALESCE((SELECT deleted_at IS NOT NULL FROM posts WHERE posts.post_id = p_post_id), FALSE),
        EXISTS(SELECT 1 FROM posts WHERE posts.post_id = p_post_id AND posts.author_user_id = p_actor_user_id),
        (SELECT posts.content FROM posts WHERE posts.post_id = p_post_id),
        (SELECT posts.image_url FROM posts WHERE posts.post_id = p_post_id)
    INTO v_post_exists, v_post_deleted, v_is_author, v_current_content, v_current_image_url;
    
    -- If post doesn't exist or is deleted, return metadata only
    IF NOT v_post_exists OR v_post_deleted THEN
        RETURN QUERY
        SELECT 
            NULL::UUID, NULL::UUID, NULL::VARCHAR(100), NULL::VARCHAR(2048),
            NULL::TEXT, NULL::VARCHAR(2048), NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ,
            v_post_exists, v_post_deleted, v_is_author;
        RETURN;
    END IF;
    
    -- If not author, return metadata only
    IF NOT v_is_author THEN
        RETURN QUERY
        SELECT 
            NULL::UUID, NULL::UUID, NULL::VARCHAR(100), NULL::VARCHAR(2048),
            NULL::TEXT, NULL::VARCHAR(2048), NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ,
            v_post_exists, v_post_deleted, v_is_author;
        RETURN;
    END IF;
    
    -- Determine new content (partial update)
    IF p_content IS NOT NULL THEN
        v_new_content := p_content;
    ELSE
        v_new_content := v_current_content;
    END IF;
    
    -- Validate new content (cannot be all whitespace)
    v_trimmed_content := TRIM(v_new_content);
    IF v_trimmed_content = '' OR v_trimmed_content IS NULL THEN
        RAISE EXCEPTION 'Content cannot be empty or all whitespace';
    END IF;
    
    -- Validate image URL if provided
    IF p_image_url IS NOT NULL THEN
        IF LENGTH(p_image_url) > 2048 THEN
            RAISE EXCEPTION 'Image URL exceeds maximum length of 2048 characters';
        END IF;
        IF p_image_url !~ '^https?://' THEN
            RAISE EXCEPTION 'Image URL must start with http:// or https://';
        END IF;
    END IF;
    
    -- Update post
    UPDATE posts
    SET 
        content = COALESCE(p_content, posts.content),
        image_url = COALESCE(p_image_url, posts.image_url),
        updated_at = now()
    WHERE posts.post_id = p_post_id;
    
    -- Return updated post with author info and metadata
    RETURN QUERY
    SELECT 
        p.post_id,
        p.author_user_id,
        u.user_name,
        u.cover_image_url,
        p.content,
        p.image_url,
        p.created_at,
        p.updated_at,
        v_post_exists,
        v_post_deleted,
        v_is_author
    FROM posts p
    INNER JOIN users u ON p.author_user_id = u.user_id
    WHERE p.post_id = p_post_id;
END;
$$;

-- sp_post_soft_delete: Soft delete a post (author only)
-- Parameters: p_actor_user_id (uuid), p_post_id (uuid)
-- Returns: metadata (post_exists, post_deleted, is_author)
CREATE OR REPLACE FUNCTION sp_post_soft_delete(
    p_actor_user_id UUID,
    p_post_id UUID
)
RETURNS TABLE(
    post_exists BOOLEAN,
    post_deleted BOOLEAN,
    is_author BOOLEAN
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_post_exists BOOLEAN;
    v_post_deleted BOOLEAN;
    v_is_author BOOLEAN;
BEGIN
    -- Check if post exists, is already deleted, and if user is author
    SELECT 
        EXISTS(SELECT 1 FROM posts WHERE posts.post_id = p_post_id),
        COALESCE((SELECT deleted_at IS NOT NULL FROM posts WHERE posts.post_id = p_post_id), FALSE),
        EXISTS(SELECT 1 FROM posts WHERE posts.post_id = p_post_id AND posts.author_user_id = p_actor_user_id)
    INTO v_post_exists, v_post_deleted, v_is_author;
    
    -- If post doesn't exist or is already deleted, return metadata
    IF NOT v_post_exists OR v_post_deleted THEN
        RETURN QUERY SELECT v_post_exists, v_post_deleted, v_is_author;
        RETURN;
    END IF;
    
    -- If not author, return metadata
    IF NOT v_is_author THEN
        RETURN QUERY SELECT v_post_exists, v_post_deleted, v_is_author;
        RETURN;
    END IF;
    
    -- Soft delete the post
    UPDATE posts
    SET 
        deleted_at = now(),
        updated_at = now()
    WHERE posts.post_id = p_post_id;
    
    -- Return success metadata
    RETURN QUERY SELECT TRUE, FALSE, TRUE;
END;
$$;

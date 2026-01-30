-- Comment Stored Procedures

-- sp_comment_list_by_post: List comments for a specific post
-- Returns: comments array with author info, total count
-- Returns empty result if post is soft-deleted (caller should check and return 404)
-- Parameters: p_post_id (required), p_limit (default 20), p_offset (default 0), p_sort (default 'oldest')
CREATE OR REPLACE FUNCTION sp_comment_list_by_post(
    p_post_id UUID,
    p_limit INTEGER DEFAULT 20,
    p_offset INTEGER DEFAULT 0,
    p_sort VARCHAR(10) DEFAULT 'oldest'
)
RETURNS TABLE(
    comment_id UUID,
    post_id UUID,
    author_user_id UUID,
    author_user_name VARCHAR(100),
    author_cover_image_url VARCHAR(2048),
    content TEXT,
    created_at TIMESTAMPTZ,
    total_count BIGINT,
    post_exists BOOLEAN,
    post_deleted BOOLEAN
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_total_count BIGINT;
    v_post_exists BOOLEAN;
    v_post_deleted BOOLEAN;
    v_order_direction TEXT;
BEGIN
    -- Validate and set order direction
    IF p_sort = 'newest' THEN
        v_order_direction := 'DESC';
    ELSE
        v_order_direction := 'ASC';  -- default to 'oldest'
    END IF;
    
    -- Check if post exists and if it's deleted
    SELECT 
        COUNT(*) > 0,
        BOOL_OR(deleted_at IS NOT NULL)
    INTO v_post_exists, v_post_deleted
    FROM posts
    WHERE posts.post_id = p_post_id;
    
    -- If post doesn't exist or is deleted, return metadata indicating this
    IF NOT v_post_exists OR v_post_deleted THEN
        RETURN QUERY
        SELECT 
            NULL::UUID,
            NULL::UUID,
            NULL::UUID,
            NULL::VARCHAR(100),
            NULL::VARCHAR(2048),
            NULL::TEXT,
            NULL::TIMESTAMPTZ,
            0::BIGINT,
            v_post_exists,
            COALESCE(v_post_deleted, FALSE);
        RETURN;
    END IF;
    
    -- Get total count of comments for this post
    SELECT COUNT(*) INTO v_total_count
    FROM comments
    WHERE comments.post_id = p_post_id;
    
    -- Return comments with author info, with dynamic ordering
    RETURN QUERY EXECUTE format('
        SELECT 
            c.comment_id,
            c.post_id,
            c.author_user_id,
            u.user_name,
            u.cover_image_url,
            c.content,
            c.created_at,
            %L::BIGINT,
            TRUE,
            FALSE
        FROM comments c
        INNER JOIN users u ON c.author_user_id = u.user_id
        WHERE c.post_id = %L
        ORDER BY c.created_at %s
        LIMIT %L
        OFFSET %L',
        v_total_count,
        p_post_id,
        v_order_direction,
        p_limit,
        p_offset
    );
END;
$$;

-- sp_comment_create: Create a new comment on a post
-- Parameters: p_actor_user_id (uuid), p_post_id (uuid), p_content (text)
-- Returns: Comment with author info, or metadata indicating post status
CREATE OR REPLACE FUNCTION sp_comment_create(
    p_actor_user_id UUID,
    p_post_id UUID,
    p_content TEXT
)
RETURNS TABLE(
    comment_id UUID,
    post_id UUID,
    author_user_id UUID,
    author_user_name VARCHAR(100),
    author_cover_image_url VARCHAR(2048),
    content TEXT,
    created_at TIMESTAMPTZ,
    post_exists BOOLEAN,
    post_deleted BOOLEAN
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_comment_id UUID;
    v_post_exists BOOLEAN;
    v_post_deleted BOOLEAN;
    v_trimmed_content TEXT;
BEGIN
    -- Validate content (required and not all whitespace)
    v_trimmed_content := TRIM(p_content);
    IF v_trimmed_content = '' OR v_trimmed_content IS NULL THEN
        RAISE EXCEPTION 'Content is required and cannot be empty';
    END IF;
    
    -- Check if post exists and if it's deleted
    SELECT 
        COUNT(*) > 0,
        BOOL_OR(deleted_at IS NOT NULL)
    INTO v_post_exists, v_post_deleted
    FROM posts
    WHERE posts.post_id = p_post_id;
    
    -- If post doesn't exist or is deleted, return metadata indicating this
    IF NOT v_post_exists OR v_post_deleted THEN
        RETURN QUERY
        SELECT 
            NULL::UUID,
            NULL::UUID,
            NULL::UUID,
            NULL::VARCHAR(100),
            NULL::VARCHAR(2048),
            NULL::TEXT,
            NULL::TIMESTAMPTZ,
            v_post_exists,
            COALESCE(v_post_deleted, FALSE);
        RETURN;
    END IF;
    
    -- Insert new comment
    INSERT INTO comments (post_id, author_user_id, content, created_at)
    VALUES (p_post_id, p_actor_user_id, p_content, now())
    RETURNING comments.comment_id INTO v_comment_id;
    
    -- Return comment with author info
    RETURN QUERY
    SELECT 
        c.comment_id,
        c.post_id,
        c.author_user_id,
        u.user_name,
        u.cover_image_url,
        c.content,
        c.created_at,
        TRUE,  -- post_exists
        FALSE  -- post_deleted
    FROM comments c
    INNER JOIN users u ON c.author_user_id = u.user_id
    WHERE c.comment_id = v_comment_id;
END;
$$;

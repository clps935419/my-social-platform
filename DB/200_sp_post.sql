-- Post Stored Procedures

-- sp_post_list: List posts (newest first, exclude soft-deleted)
-- Returns: posts array with author info, total count
-- Parameters: p_limit (default 20), p_offset (default 0)
CREATE OR REPLACE FUNCTION sp_post_list(
    p_limit INTEGER DEFAULT 20,
    p_offset INTEGER DEFAULT 0
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
    -- Get total count of non-deleted posts
    SELECT COUNT(*) INTO v_total_count
    FROM posts
    WHERE deleted_at IS NULL;
    
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
    ORDER BY p.created_at DESC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$;

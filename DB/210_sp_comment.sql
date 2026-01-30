-- Comment Stored Procedures

-- sp_comment_list_by_post: List comments for a specific post
-- Returns: comments array with author info, total count
-- Returns empty result if post is soft-deleted (caller should check and return 404)
-- Parameters: p_post_id (required), p_limit (default 20), p_offset (default 0)
CREATE OR REPLACE FUNCTION sp_comment_list_by_post(
    p_post_id UUID,
    p_limit INTEGER DEFAULT 20,
    p_offset INTEGER DEFAULT 0
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
BEGIN
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
    
    -- Return comments with author info
    RETURN QUERY
    SELECT 
        c.comment_id,
        c.post_id,
        c.author_user_id,
        u.user_name,
        u.cover_image_url,
        c.content,
        c.created_at,
        v_total_count,
        TRUE,  -- post_exists
        FALSE  -- post_deleted
    FROM comments c
    INNER JOIN users u ON c.author_user_id = u.user_id
    WHERE c.post_id = p_post_id
    ORDER BY c.created_at ASC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$;

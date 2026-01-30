-- Minimal Seed Data
-- At least 1 user, 2 posts, multiple comments for US1 independent verification

-- Seed user (password: "password123" - for testing only)
-- Password hash generated with salt "testsalt123" (must match backend hashing logic)
INSERT INTO users (user_id, phone_e164, user_name, email, password_hash, password_salt, biography, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000001', '+886912345678', 'Test User 1', 'test1@example.com', 
     'will_be_replaced_by_backend_hash', 'testsalt123', 'Hello, I am a test user!', 
     NOW() AT TIME ZONE 'UTC'),
    ('00000000-0000-0000-0000-000000000002', '+886923456789', 'Test User 2', 'test2@example.com',
     'will_be_replaced_by_backend_hash', 'testsalt456', 'Another test user here.',
     NOW() AT TIME ZONE 'UTC');

-- Seed posts
INSERT INTO posts (post_id, author_user_id, content, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000001',
     'This is the first test post. Welcome to our social platform!',
     NOW() AT TIME ZONE 'UTC' - INTERVAL '2 hours'),
    ('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000002',
     'Second post here! Testing the platform features.',
     NOW() AT TIME ZONE 'UTC' - INTERVAL '1 hour'),
    ('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000001',
     'Third post with some sample content for testing.',
     NOW() AT TIME ZONE 'UTC' - INTERVAL '30 minutes');

-- Seed comments (multiple comments per post)
INSERT INTO comments (comment_id, post_id, author_user_id, content, created_at)
VALUES
    -- Comments on first post
    ('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000101',
     '00000000-0000-0000-0000-000000000002', 'Great first post!',
     NOW() AT TIME ZONE 'UTC' - INTERVAL '1 hour 50 minutes'),
    ('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000101',
     '00000000-0000-0000-0000-000000000001', 'Thanks for the feedback!',
     NOW() AT TIME ZONE 'UTC' - INTERVAL '1 hour 40 minutes'),
    ('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000101',
     '00000000-0000-0000-0000-000000000002', 'Looking forward to more posts.',
     NOW() AT TIME ZONE 'UTC' - INTERVAL '1 hour 30 minutes'),
    
    -- Comments on second post
    ('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000102',
     '00000000-0000-0000-0000-000000000001', 'Nice post! Keep it up.',
     NOW() AT TIME ZONE 'UTC' - INTERVAL '50 minutes'),
    ('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000102',
     '00000000-0000-0000-0000-000000000002', 'Thank you!',
     NOW() AT TIME ZONE 'UTC' - INTERVAL '45 minutes'),
    
    -- Comments on third post
    ('00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000103',
     '00000000-0000-0000-0000-000000000002', 'Interesting content!',
     NOW() AT TIME ZONE 'UTC' - INTERVAL '25 minutes');

-- Note: Password hashes will need to be updated by backend on first run or during registration
-- For testing, you can use the register endpoint to create proper users

# Data Model (Phase 1)

**Feature**: [spec.md](spec.md)  
**Branch**: `001-social-platform`  
**Date**: 2026-01-30

本資料模型以「三天 MVP + 可驗收」為優先，並符合 SP-first：所有資料操作都會由 Stored Procedure 封裝。

## Entities

### User

**Fields**
- `user_id` (uuid / bigserial)：主鍵
- `phone_e164` (varchar)：手機號碼（E.164 正規化後）；唯一
- `user_name` (varchar)：顯示名稱
- `email` (varchar, nullable)
- `password_hash` (varchar)
- `password_salt` (varchar)
- `cover_image_url` (varchar(2048), nullable)
- `biography` (text, nullable)
- `created_at` (timestamptz)
- `updated_at` (timestamptz)

**Validation rules**
- `phone_e164`：必填、符合 E.164；輸入允許空白/破折號/括號但需正規化
- `cover_image_url`：若提供，需 http/https 且長度 <= 2048

### Post

**Fields**
- `post_id` (uuid / bigserial)：主鍵
- `author_user_id`：外鍵 -> User
- `content` (text)：必填
- `image_url` (varchar(2048), nullable)：http/https
- `created_at` (timestamptz)
- `updated_at` (timestamptz)
- `deleted_at` (timestamptz, nullable)：軟刪除

**State**
- Active：`deleted_at IS NULL`
- Deleted：`deleted_at IS NOT NULL`

**Validation rules**
- `image_url`：若提供，需 http/https 且長度 <= 2048

### Comment

**Fields**
- `comment_id` (uuid / bigserial)：主鍵
- `post_id`：外鍵 -> Post
- `author_user_id`：外鍵 -> User
- `content` (text)：必填
- `created_at` (timestamptz)

**Rules**
- 若 `post.deleted_at IS NOT NULL`：
  - `GET /posts/{postId}/comments` 必須回 404
  - `POST /posts/{postId}/comments` 必須回 404

### RefreshToken

**Fields**
- `refresh_token_id` (uuid / bigserial)：主鍵
- `user_id`：外鍵 -> User
- `token_hash` (varchar)：refresh token 的雜湊
- `issued_at` (timestamptz)
- `expires_at` (timestamptz)
- `revoked_at` (timestamptz, nullable)
- `replaced_by_token_id` (nullable)：rotation 時可記錄新 token（選配）

**Rules (Rotation)**
- 每次 refresh：
  - 驗證舊 token 未撤銷且未過期
  - 原子性地撤銷舊 token（或標記 replaced）
  - 產生新 token（新 row）

## Relationships

- User 1..* Post
- User 1..* Comment
- Post 1..* Comment
- User 1..* RefreshToken

## Indexes (suggested)

- `users(phone_e164)` unique
- `posts(deleted_at)` (for filtering) + `posts(created_at desc)` (for list)
- `comments(post_id, created_at)`
- `refresh_tokens(user_id)` + `refresh_tokens(token_hash)` unique

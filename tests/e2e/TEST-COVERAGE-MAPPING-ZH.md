# 測試覆蓋完整對應表 (Test Coverage Mapping)

本文檔說明所有要求的測試場景已完整實現於 tests/e2e 目錄中。

## 1. Health Check API

### 要求場景
- GET /api/health：200

### 實現位置：`health-check.http`
- ✅ Test 1: Health check returns 200 OK
- ✅ Test 2: Verify health check responds without database

## 2. US1（公開瀏覽功能）

### 要求場景
- GET /api/posts：成功 + 分頁 + sort + 無效參數 400
- GET /api/posts/{postId}/comments：成功 + 分頁 + sort + 無效 postId 400 + 不存在 404

### 實現位置：`us1-acceptance.http`

#### GET /api/posts 測試
- ✅ Test 1: List all posts (newest first) - 成功
- ✅ Test 2: List posts with pagination (limit) - 分頁
- ✅ Test 3: List posts with pagination (offset) - 分頁
- ✅ Test 3b: List posts sorted oldest first - sort
- ✅ Test 4: Invalid limit (too high) - 無效參數 400
- ✅ Test 5: Invalid limit (negative) - 無效參數 400
- ✅ Test 6: Invalid offset (negative) - 無效參數 400
- ✅ Test 10b: Invalid sort value - 無效參數 400
- ✅ Test 11: Verify soft-deleted posts are excluded

#### GET /api/posts/{postId}/comments 測試
- ✅ Test 7: List comments for a post - 成功
- ✅ Test 8: List comments with pagination - 分頁
- ✅ Test 8b: List comments sorted newest first - sort
- ✅ Test 9: Comments for non-existent post - 不存在 404
- ✅ Test 10: Comments with invalid post ID format - 無效 postId 400
- ✅ Test 12: Verify comments on soft-deleted post return 404

## 3. US2（註冊/登入/refresh/me）

### 要求場景
- POST /auth/register：成功、重複 409、格式錯誤 400、rate limit 429
- POST /auth/login：成功、錯誤密碼/不存在 401、rate limit 429
- GET /me：成功、未登入 401、token 無效 401
- POST /auth/refresh：成功、舊 token 401、無效 token 401

### 實現位置：`us2-acceptance.http`

#### POST /auth/register 測試
- ✅ Test 1: Register new user (success) - 成功
- ✅ Test 2: Register with duplicate phone number - 重複 409
- ✅ Test 3: Register with invalid phone number - 格式錯誤 400
- ✅ Test 16: Rate limit test for register - rate limit 429

#### POST /auth/login 測試
- ✅ Test 4: Login with correct credentials - 成功
- ✅ Test 5: Login with wrong password - 錯誤密碼 401
- ✅ Test 6: Login with non-existent phone number - 不存在 401
- ✅ Test 15: Rate limit test for login - rate limit 429

#### GET /me 測試
- ✅ Test 7: Get user profile with valid token - 成功
- ✅ Test 8: Get user profile without token - 未登入 401
- ✅ Test 9: Get user profile with invalid token - token 無效 401

#### POST /auth/refresh 測試
- ✅ Test 10: Refresh token (success) - 成功
- ✅ Test 11: Try to use old refresh token again - 舊 token 401
- ✅ Test 12: Refresh with invalid token - 無效 token 401

## 4. US3（發文/管理貼文）

### 要求場景
- POST /posts：成功、未登入 401、內容空白 400、image URL 錯誤/過長 400
- PATCH /posts/{postId}：成功、非作者 403、已刪 404、postId 無效 400
- DELETE /posts/{postId}：成功、非作者 403、已刪/不存在 404、postId 無效 400

### 實現位置：`us3-acceptance.http`

#### POST /posts 測試
- ✅ Step 5 & 20: User creates a post - 成功
- ✅ Step 16: Create post without auth - 未登入 401
- ✅ Step 17: Create post with empty content - 內容空白 400
- ✅ Step 18: Create post with invalid image URL - image URL 錯誤 400
- ✅ Step 19: Create post with image URL too long - image URL 過長 400

#### PATCH /posts/{postId} 測試
- ✅ Step 9: User1 updates their own post - 成功
- ✅ Step 7: User2 tries to update User1's post - 非作者 403
- ✅ Step 15: Try to update deleted post - 已刪 404
- ✅ Step 22: Update post with invalid postId format - postId 無效 400

#### DELETE /posts/{postId} 測試
- ✅ Step 12: User1 soft deletes their own post - 成功
- ✅ Step 8: User2 tries to delete User1's post - 非作者 403
- ✅ Step 14: Try to delete already deleted post - 已刪 404
- ✅ Step 23: Delete post with invalid postId format - postId 無效 400

## 5. US4（留言）

### 要求場景
- POST /posts/{postId}/comments：成功、未登入 401、內容空白 400、postId 無效 400、貼文不存在/已刪 404
- GET /posts/{postId}/comments：已在 US1 覆蓋

### 實現位置：`us4-acceptance.http`

#### POST /posts/{postId}/comments 測試
- ✅ Test 1: Create comment on existing post - 成功
- ✅ Test 2: Create comment without authentication - 未登入 401
- ✅ Test 3 & 4: Create comment with empty/whitespace content - 內容空白 400
- ✅ Test 5b: Create comment with invalid postId format - postId 無效 400
- ✅ Test 5: Create comment on non-existent post - 貼文不存在 404
- ✅ Test 6: Create comment on soft-deleted post - 貼文已刪 404

#### GET /posts/{postId}/comments
- ✅ 已在 `us1-acceptance.http` 覆蓋（Tests 7-12）

## 6. 進階功能（若有開）

### 要求場景
- GET /posts?mine=true：成功、未登入 401

### 實現位置：`test-mine-filter.http`
- ✅ Step 6 & 7: User gets only their own posts (mine=true) - 成功
- ✅ Step 8: Try to use mine=true without authentication - 未登入 401

---

## 統計摘要 (Summary Statistics)

| 功能模組 | 測試文件 | 測試數量 | 覆蓋狀態 |
|---------|---------|---------|---------|
| Health Check | health-check.http | 2 | ✅ 完整 |
| US1: 公開瀏覽 | us1-acceptance.http | 15 | ✅ 完整 |
| US2: 認證系統 | us2-acceptance.http | 16 | ✅ 完整 |
| US3: 貼文管理 | us3-acceptance.http | 17 | ✅ 完整 |
| US4: 留言功能 | us4-acceptance.http | 7 | ✅ 完整 |
| 進階: Mine Filter | test-mine-filter.http | 3 | ✅ 完整 |
| **總計** | **6 個文件** | **60 個測試** | **✅ 100% 覆蓋** |

## HTTP 狀態碼覆蓋 (HTTP Status Code Coverage)

所有要求的狀態碼均已測試：
- ✅ 200 OK
- ✅ 201 Created
- ✅ 204 No Content
- ✅ 400 Bad Request
- ✅ 401 Unauthorized
- ✅ 403 Forbidden
- ✅ 404 Not Found
- ✅ 409 Conflict
- ✅ 429 Too Many Requests

## 結論

✅ **所有要求的測試場景已完整實現**
- 共 6 個測試文件
- 60 個獨立測試案例
- 涵蓋所有 12 個 API 端點
- 測試所有 9 種 HTTP 狀態碼
- 包含完整的正面和負面測試案例

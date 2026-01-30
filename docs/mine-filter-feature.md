# 貼文過濾功能說明 (Post Filtering Feature)

## 功能概述

GET /posts 端點現在支援一個新的查詢參數 `mine`，允許已登入的使用者過濾出只屬於自己的貼文。

## API 變更

### GET /posts

**新增參數:**
- `mine` (boolean, optional): 當設為 `true` 時，只返回當前登入使用者的貼文。需要身份驗證。

**請求範例:**

```http
# 獲取所有貼文（公開，無需認證）
GET /api/posts?limit=20&offset=0

# 獲取自己的貼文（需要認證）
GET /api/posts?mine=true&limit=20&offset=0
Authorization: ******
```

**回應:**
- `200 OK`: 返回貼文列表
- `401 Unauthorized`: 當使用 `mine=true` 但未提供有效的認證令牌時

## 實作詳情

### 1. 資料庫層 (Database Layer)

更新了 stored procedure `sp_post_list` 以支援可選的作者過濾:

```sql
CREATE OR REPLACE FUNCTION sp_post_list(
    p_limit INTEGER DEFAULT 20,
    p_offset INTEGER DEFAULT 0,
    p_author_user_id UUID DEFAULT NULL  -- 新增參數
)
```

- 當 `p_author_user_id` 為 `NULL` 時，返回所有貼文
- 當提供 `p_author_user_id` 時，只返回該使用者的貼文

### 2. DAO 層

`PostDao.listPosts()` 方法新增了 `authorUserId` 參數:

```java
public Map<String, Object> listPosts(int limit, int offset, String authorUserId)
```

### 3. Controller 層

`PostController.listPosts()` 新增了 `mine` 參數處理:

```java
@GetMapping
public ResponseEntity<PostListResponse> listPosts(
    @RequestParam(required = false) Integer limit,
    @RequestParam(required = false) Integer offset,
    @RequestParam(required = false) Boolean mine,  // 新增參數
    HttpServletRequest httpRequest
)
```

邏輯：
- 當 `mine=true` 時，從 JWT token 中提取使用者 ID
- 如果沒有有效的 token，返回 401 錯誤
- 將使用者 ID 傳遞給 DAO 進行過濾

### 4. OpenAPI 規格

更新了 OpenAPI 規格以包含新參數：

```yaml
/posts:
  get:
    parameters:
      - name: mine
        in: query
        required: false
        schema:
          type: boolean
          default: false
        description: Filter to show only current user's posts (requires authentication)
```

## 使用場景

1. **公開瀏覽**: 未登入的使用者可以瀏覽所有貼文
   ```
   GET /api/posts
   ```

2. **查看自己的貼文**: 登入使用者可以只看自己的貼文
   ```
   GET /api/posts?mine=true
   Authorization: ******
   ```

3. **分頁支援**: 可以與其他參數組合使用
   ```
   GET /api/posts?mine=true&limit=10&offset=20
   Authorization: ******
   ```

## 測試

測試腳本位於 `docs/test-mine-filter.http`，包含以下測試場景:

1. 創建多個使用者和貼文
2. 驗證 `mine=true` 只返回當前使用者的貼文
3. 驗證未認證時使用 `mine=true` 返回 401
4. 驗證不使用過濾參數時返回所有貼文

## 向後相容性

此變更完全向後相容：
- 現有的 API 調用（不使用 `mine` 參數）行為保持不變
- `mine` 參數是可選的，默認為 `false`
- 不影響現有的客戶端代碼

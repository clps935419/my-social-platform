# Hey API Function Name Mapping

## 問題 (Issue)

Build error 顯示無法找到 import 的函式，例如：
- `postAuthLogin` not found
- `postAuthRegister` not found
- `useGetPosts` not found
- 等等...

## 原因 (Root Cause)

Hey API `@hey-api/openapi-ts` 產生的函式名稱與我原本使用的名稱不一致。產生的函式使用 **operationId** 或根據 HTTP 方法與路徑生成的簡化名稱。

## 解決方案 (Solution)

更新所有 import 以使用正確的產生函式名稱。

## 函式對應表 (Function Mapping)

### SDK Functions (sdk.gen.ts)

| 舊名稱 (錯誤) | 新名稱 (正確) | 說明 |
|--------------|--------------|------|
| `postAuthLogin` | `login` | 登入 |
| `postAuthRegister` | `register` | 註冊 |
| `postAuthRefresh` | `refresh` | 刷新 token |
| `postPosts` | `createPost` | 建立貼文 |
| `postPostsPostIdComments` | `createComment` | 建立留言 |
| `patchPostsPostId` | `updatePost` | 更新貼文 |
| `deletePostsPostId` | `deletePost` | 刪除貼文 |
| `getPostsPostId` | *(未使用)* | 取得單一貼文 |
| `getMe` | `getProfile` | 取得個人資料 |

### TanStack Query Hooks (@tanstack/vue-query.gen.ts)

| 舊名稱 (錯誤) | 新名稱 (正確) | 說明 |
|--------------|--------------|------|
| `useGetPosts` | `listPostsOptions` + `useQuery` | 貼文列表 Query |
| `useGetPostsPostIdComments` | `listCommentsOptions` + `useQuery` | 留言列表 Query |

## 使用方式變更

### Before (錯誤) ❌
```typescript
// AuthDialog.vue
import { postAuthLogin, postAuthRegister } from '../api/generated/sdk.gen';
const response = await postAuthLogin({ body: { ... } });

// PostsPage.vue
import { useGetPosts } from '../api/generated/@tanstack/vue-query.gen';
const { data } = useGetPosts({ query: { limit, offset } });
```

### After (正確) ✅
```typescript
// AuthDialog.vue
import { login, register } from '../api/generated/sdk.gen';
const response = await login({ body: { ... } });

// PostsPage.vue
import { listPostsOptions } from '../api/generated/@tanstack/vue-query.gen';
import { useQuery } from '@tanstack/vue-query';
const queryOptions = computed(() => listPostsOptions({ query: { limit, offset: offset.value } }));
const { data } = useQuery(queryOptions);
```

## 修正的檔案 (Fixed Files)

1. `frontend/src/components/AuthDialog.vue`
   - `login`, `register`

2. `frontend/src/components/CreateCommentForm.vue`
   - `createComment`

3. `frontend/src/components/CreatePostForm.vue`
   - `createPost`

4. `frontend/src/components/PostActions.vue`
   - `updatePost`, `deletePost`

5. `frontend/src/components/PostCard.vue`
   - `listCommentsOptions` + `useQuery`

6. `frontend/src/pages/PostsPage.vue`
   - `listPostsOptions` + `useQuery` (with computed reactive options)

7. `frontend/src/api/client.ts`
   - `refresh`
   - 類型從 `RefreshResponse` → `RefreshResponse2`

8. `frontend/src/queries/me.ts`
   - `getProfile`

## 額外改進 (Additional Improvements)

### PostsPage.vue - 響應式 Query

為了讓分頁正確響應式更新，將 query options 包裝在 `computed()` 中：

```typescript
const queryOptions = computed(() => listPostsOptions({
  query: {
    limit,
    offset: offset.value,  // 響應式值
  },
}));

const { data, isLoading, error } = useQuery(queryOptions);
```

這樣當 `offset.value` 改變時，query 會自動重新執行。

## 驗證 (Verification)

Build error 應該已完全解決：
- ✅ 所有函式都從 `sdk.gen.ts` 正確 import
- ✅ 所有 TanStack Query hooks 使用正確的 options 函式
- ✅ 類型定義都正確對應

## Commit

```
af43473 - fix: Update imports to match Hey API generated function names
```

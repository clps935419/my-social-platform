# API Response Structure Fix

## 問題 (Issue)

使用者指出「POST API 有拿到資料但沒正確顯示」，要求全面檢查是否使用 Hey API 產生的型別。

經檢查發現：
1. 使用錯誤的扁平結構存取資料 (如 `post.authorUserId`)
2. 列表回應使用錯誤的欄位名稱 (`posts`/`comments` 而非 `items`)
3. Query key 不符合 Hey API 的 pattern

## 原因 (Root Cause)

沒有完全遵循 OpenAPI spec 定義的資料結構。API 實際返回：
- **嵌套結構**: `Post` 和 `Comment` 有 `author` 物件
- **統一欄位**: 列表回應都用 `items` 欄位
- **特定命名**: 使用 `image` 而非 `imageUrl`

## 解決方案 (Solution)

### 1. Post 資料結構修正

#### Before ❌
```vue
<!-- PostCard.vue -->
<span>{{ post.authorUserName }}</span>
<el-image v-if="post.imageUrl" :src="post.imageUrl" />

<!-- PostActions props -->
<PostActions
  :author-user-id="post.authorUserId"
  :current-image-url="post.imageUrl"
/>
```

#### After ✅
```vue
<!-- PostCard.vue -->
<span>{{ post.author?.userName || 'Unknown' }}</span>
<el-image v-if="post.image" :src="post.image" />

<!-- PostActions props -->
<PostActions
  :author-user-id="post.author!.userId!"
  :current-image-url="post.image"
/>
```

### 2. Comment 資料結構修正

#### Before ❌
```vue
<!-- CommentsSection.vue -->
<span class="comment-user">{{ comment.authorUserName }}:</span>
```

#### After ✅
```vue
<!-- CommentsSection.vue -->
<span class="comment-user">{{ comment.author?.userName || 'Unknown' }}:</span>
```

### 3. 列表回應欄位修正

#### Before ❌
```typescript
// PostsPage.vue
const posts = computed(() => data.value?.data?.posts ?? []);

// PostCard.vue
const comments = computed(() => commentsData.value?.data?.comments ?? []);
```

#### After ✅
```typescript
// PostsPage.vue
const posts = computed(() => data.value?.data?.items ?? []);

// PostCard.vue
const comments = computed(() => commentsData.value?.data?.items ?? []);
```

### 4. Query Key 修正

#### Before ❌
```typescript
// CreatePostForm.vue
queryClient.invalidateQueries({ queryKey: ['posts'] });

// CreateCommentForm.vue
queryClient.invalidateQueries({
  queryKey: ['posts', props.postId, 'comments'],
});

// PostActions.vue
queryClient.invalidateQueries({ queryKey: ['posts'] });
```

#### After ✅
```typescript
// CreatePostForm.vue
queryClient.invalidateQueries({ queryKey: ['listPosts'] });

// CreateCommentForm.vue
queryClient.invalidateQueries({
  queryKey: ['listComments'],
});

// PostActions.vue
queryClient.invalidateQueries({ queryKey: ['listPosts'] });
```

## API 型別定義 (From OpenAPI)

### Post Structure
```typescript
export type Post = {
  postId?: string;
  author?: Author;      // ✅ 嵌套物件
  content?: string;
  image?: string;       // ✅ 不是 imageUrl
  createdAt?: string;
  updatedAt?: string;
};
```

### Author Structure
```typescript
export type Author = {
  userId?: string;
  userName?: string;
  coverImage?: string;
};
```

### Comment Structure
```typescript
export type Comment = {
  commentId?: string;
  postId?: string;
  author?: Author;      // ✅ 嵌套物件
  content?: string;
  createdAt?: string;
};
```

### List Response Structures
```typescript
export type PostListResponse = {
  items?: Array<Post>;  // ✅ 統一使用 items
  total?: number;
};

export type CommentListResponse = {
  items?: Array<Comment>;  // ✅ 統一使用 items
  total?: number;
};
```

## 欄位對應表 (Field Mapping)

### Post Fields
| 舊欄位 (錯誤) | 新欄位 (正確) | 型別 |
|--------------|--------------|------|
| `post.authorUserId` | `post.author?.userId` | `string \| undefined` |
| `post.authorUserName` | `post.author?.userName` | `string \| undefined` |
| `post.imageUrl` | `post.image` | `string \| undefined` |

### Comment Fields
| 舊欄位 (錯誤) | 新欄位 (正確) | 型別 |
|--------------|--------------|------|
| `comment.authorUserName` | `comment.author?.userName` | `string \| undefined` |

### List Response Fields
| 舊欄位 (錯誤) | 新欄位 (正確) | 型別 |
|--------------|--------------|------|
| `data?.posts` | `data?.items` | `Array<Post> \| undefined` |
| `data?.comments` | `data?.items` | `Array<Comment> \| undefined` |

## 修正的檔案 (Fixed Files)

1. **frontend/src/pages/PostsPage.vue**
   - `data?.posts` → `data?.items`

2. **frontend/src/components/PostCard.vue**
   - `post.authorUserId` → `post.author?.userId`
   - `post.authorUserName` → `post.author?.userName`
   - `post.imageUrl` → `post.image`
   - `commentsData?.comments` → `commentsData?.items`

3. **frontend/src/components/CommentsSection.vue**
   - `comment.authorUserName` → `comment.author?.userName`

4. **frontend/src/components/CreatePostForm.vue**
   - Query key: `['posts']` → `['listPosts']`

5. **frontend/src/components/CreateCommentForm.vue**
   - Query key: `['posts', postId, 'comments']` → `['listComments']`

6. **frontend/src/components/PostActions.vue**
   - Query key: `['posts']` → `['listPosts']`

## 可選鏈 (Optional Chaining) 使用

由於所有欄位在 TypeScript 中都是 optional (`?:`), 使用可選鏈確保安全：

```typescript
// ✅ 安全存取
post.author?.userName || 'Unknown'
post.author?.userId
post.image

// ✅ 非空斷言 (當確定值存在時)
post.postId!
post.author!.userId!
```

## 驗證 (Verification)

修正後應該能夠：
- ✅ 正確顯示貼文列表與作者名稱
- ✅ 正確顯示貼文圖片
- ✅ 正確顯示留言與留言者名稱
- ✅ 新增貼文後列表自動更新
- ✅ 新增留言後留言區自動更新
- ✅ 編輯/刪除貼文後列表自動更新

## Query Key Pattern

Hey API 生成的 query key 使用特定格式：

```typescript
// 生成的 query key 函式
export const listPostsQueryKey = (options?) => [
  createQueryKey('listPosts', options)
];

export const listCommentsQueryKey = (options) => [
  createQueryKey('listComments', options)
];

// 使用時
queryClient.invalidateQueries({ queryKey: ['listPosts'] });
queryClient.invalidateQueries({ queryKey: ['listComments'] });
```

這確保了 query 快取的正確失效與更新。

## Commit

```
4e96514 - fix: Use correct API response structure with nested author and items fields
```

## 總結

這次修正確保了：
1. 完全符合 OpenAPI spec 定義的資料結構
2. 使用 Hey API 生成的型別定義
3. 正確的嵌套物件存取 (`author` 物件)
4. 統一的列表回應欄位 (`items`)
5. 正確的 query key pattern

現在所有 API 回應資料都能正確顯示和更新。

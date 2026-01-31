# UI Prototype Alignment Refactor

## 問題 (Issue)

使用者指出實作中使用了路由導航到獨立的貼文詳情頁，但 `ui-prototype.html` 是單頁設計，沒有路由。

## 解決方案 (Solution)

將應用重構為完全符合 ui-prototype.html 的單頁滾動設計：

### Before (之前) ❌
```
/ (PostsPage)
  - 顯示貼文列表
  - 點擊「查看完整內容與留言」→ 跳轉到 /posts/:postId

/posts/:postId (PostDetailPage)
  - 顯示單一貼文
  - 顯示該貼文的所有留言
  - 有「返回列表」按鈕
```

### After (現在) ✅
```
/ (PostsPage)
  - 顯示貼文列表
  - 每個貼文直接顯示留言區
  - 可以在同一頁面查看所有內容
  - 無需路由跳轉
```

## 檔案變更 (File Changes)

### 刪除的檔案
- ❌ `frontend/src/pages/PostDetailPage.vue` - 不再需要獨立詳情頁

### 修改的檔案

#### 1. `PostCard.vue`
**Before:**
- 顯示貼文內容
- 底部有「查看完整內容與留言」按鈕
- emit `view-detail` 事件觸發路由跳轉

**After:**
- 顯示貼文內容
- **直接內嵌 CommentsSection 元件**
- **使用 useGetPostsPostIdComments hook 載入留言**
- **內嵌 CreateCommentForm 讓登入使用者留言**
- 訪客顯示「登入後即可參與討論」提示

#### 2. `router.ts`
**Before:**
```typescript
routes: [
  { path: '/', name: 'posts', component: PostsPage },
  { path: '/posts/:postId', name: 'post-detail', component: PostDetailPage }
]
```

**After:**
```typescript
routes: [
  { path: '/', name: 'posts', component: PostsPage }
]
```

#### 3. `PostsPage.vue`
**Before:**
- `goToPostDetail()` 函數執行 `router.push()`
- PostCard 監聽 `@view-detail` 事件

**After:**
- 移除 `goToPostDetail()` 函數
- PostCard 監聽 `@login-required` 事件（用於開啟登入對話框）
- 新增 `AuthDialog` 元件以支援訪客點擊登入連結

#### 4. `App.vue`
**Before:**
```typescript
function goHome() {
  router.push({ name: 'posts' });
}
```

**After:**
```typescript
function goHome() {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}
```

#### 5. `CommentsSection.vue`
- 移除 `formatRelativeTime` 的使用（簡化顯示）
- 新增 `isLoading` prop 以顯示載入狀態
- 移除留言時間戳顯示（匹配 prototype）

## 使用者體驗改善 (UX Improvements)

### Before ❌
1. 看到貼文列表
2. 點擊「查看完整內容與留言」
3. 跳轉到新頁面 (URL 改變)
4. 看到留言
5. 需要點擊「返回列表」回到首頁

### After ✅
1. 看到貼文列表
2. **直接看到每個貼文的留言**
3. **在同一頁面滾動查看所有內容**
4. **無需跳轉或返回**

## 符合原型 (Matches Prototype)

現在實作完全符合 `ui-prototype.html` 的行為：
- ✅ 單頁應用，無路由跳轉
- ✅ 所有貼文與留言顯示在同一頁面
- ✅ 滾動瀏覽所有內容
- ✅ 登入後直接在貼文下方留言
- ✅ 訪客看到登入提示

## Commit

```
3261968 - refactor: Remove routing, display posts+comments on single page per ui-prototype
```

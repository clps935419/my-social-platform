---
description: "Task list for feature implementation"
---

# Tasks: 簡易社群媒體平台（手機號碼註冊/登入、貼文、留言）

**Input**: 設計文件位於 `specs/001-social-platform/`
- 必讀：`plan.md`、`spec.md`
- 參考：`research.md`、`data-model.md`、`quickstart.md`、`contracts/openapi.yaml`、`appendix-technical-contract.md`

**Tech Stack (from plan.md)**: Nginx + Vue 3/Vite + Spring Boot (Java 17) + PostgreSQL 16 (SP-first)

**Important Constraints (must be enforced)**
- DB 存取：SP-first；後端以 JdbcTemplate/SimpleJdbcCall 參數化呼叫；禁止 ORM/JPA Repository 直 CRUD；禁止 SQL 字串拼接。
- 部署：Docker Compose 一鍵啟動 nginx/app/db；Nginx 反代 `/api/*`。
- Swagger UI：對外固定路徑 `/api/swagger-ui/index.html`。
- 安全：密碼 salt+hash；避免 SQLi/XSS；錯誤回應不得洩漏 SQL/stack trace。
- 時間：UTC、ISO 8601（含 `Z`）。

## Format

每個任務都必須以 markdown checkbox 開頭，並包含 TaskID 與（必要時）標記：

格式（不要複製成任務行）：`- [ ] T### [P?] [US?] 描述（含檔案路徑）`

- `[P]`：可並行（不同檔案、無未完成依賴）
- `[US#]`：只用在 User Story phases（US1~US4）

---

## Phase 1: Setup（專案初始化 / 目錄與一鍵啟動骨架）

- [X] T001 建立目錄結構（DB/、backend/、frontend/、nginx/）於 `DB/README.md`
- [X] T002 建立 Docker Compose 骨架（nginx/app/db + networks/volumes + env；後端需可「只用 Docker」啟動，不依賴本機 Java）於 `docker-compose.yml`
- [X] T003 [P] 建立 Nginx 站台設定（靜態站 + `/api/*` 反代；保留 `/api` base path 不做 rewrite；確保 `/api/swagger-ui/index.html` 可用）於 `nginx/default.conf`
- [X] T004 [P] 建立 DB init 掛載與腳本順序（docker-entrypoint-initdb.d）於 `docker-compose.yml`
- [X] T005 [P] 先建立 Biome 規則檔（biome.json）；工具安裝與 npm scripts（format/lint）待 T007 前端骨架完成後補齊
- [X] T006 [P] 建立後端 Spring Boot 專案骨架（Maven + Web/JDBC/Validation + springdoc-openapi）於 `backend/pom.xml`
- [X] T007 [P] 建立前端 Vue 3 + Vite 專案骨架（含 dev/build scripts）於 `frontend/package.json`
- [X] T008 建立環境變數樣板（DB/app secrets）於 `.env.example`
- [X] T063 建立 Makefile 指令（build/up/down/logs/ps/clean 等；統一呼叫 docker compose）於 `Makefile`

---

## Phase 2: Foundational（阻塞性基礎設施；完成後才能開始任何 US）

### Database（Schema/Indexes；SP 會分散到各 User Story）

- [X] T009 建立 DB schema/extension 基礎（UTC/timestamptz 使用）於 `DB/001_schema.sql`
- [X] T010 建立核心資料表（users/posts/comments/refresh_tokens）於 `DB/010_tables.sql`
- [X] T011 建立索引與唯一約束（phone_e164 unique、token_hash unique、comments(post_id,created_at) 等）於 `DB/020_indexes.sql`
- [X] T012 建立最小 seed（供 US1 可獨立驗收：至少 1 user/2 posts/多 comments）於 `DB/030_seed.sql`

### Backend（共用框架：錯誤/驗證/DAO/SP 呼叫/Swagger）

- [X] T013 建立後端設定（datasource 走 env；關閉 stack trace/whitelabel）於 `backend/src/main/resources/application.yml`
- [X] T014 實作統一錯誤回應（Error{errorCode,message}）與例外映射於 `backend/src/main/java/com/example/platform/common/ErrorResponse.java`
- [X] T015 實作全域例外處理（Validation/BadRequest/Unauthorized/Forbidden/NotFound/Conflict/TooManyRequests；不洩漏 SQL/stack）於 `backend/src/main/java/com/example/platform/common/GlobalExceptionHandler.java`
- [X] T016 建立 Stored Procedure 呼叫基礎封裝（SimpleJdbcCall + named params；禁止 SQL 拼接）於 `backend/src/main/java/com/example/platform/dao/StoredProcedureExecutor.java`
- [X] T017 建立 controller 層共用輸入驗證（limit/offset default/max、URL 長度、E.164 normalize helper 介面）於 `backend/src/main/java/com/example/platform/api/validation/RequestValidators.java`
- [X] T018 設定 Swagger/OpenAPI 對外固定路徑可用（後端 base path 固定為 `/api`；springdoc 路徑採預設並可經 nginx 對外存取 `/api/swagger-ui/index.html`）於 `backend/src/main/resources/application.yml`
- [X] T064 建立後端容器 dev 啟動/熱更新機制（不需本機 Java；以「容器內 Maven 執行 + spring-boot-devtools + source volume mount」為準；修改程式碼可自動重新編譯並重啟）於 `docker-compose.yml`
- [X] T065 建立 Health API（不需 DB、無副作用；用於驗證後端確實在跑；回應含 status 與 UTC time）於 `backend/src/main/java/com/example/platform/api/HealthController.java`

### Frontend（共用框架：API client / Query client）

- [X] T019 建立 API client（baseURL=/api；統一錯誤處理；預留加 Authorization header）於 `frontend/src/api/http.ts`
- [X] T020 建立 TanStack Query client 與 Provider（QueryClient + error boundary 最小化）於 `frontend/src/main.ts`

**Checkpoint**：`docker compose up -d --build` 後可打開 `http://localhost/api/swagger-ui/index.html`（即使 endpoints 尚未全實作也需能進 Swagger UI）。

---

## Phase 3: User Story 1 - 訪客可瀏覽貼文與留言 (Priority: P1) 🎯 MVP

**Goal**：訪客可 `GET /api/posts`、`GET /api/posts/{postId}/comments` 並看到 seed 資料（排除軟刪除貼文）。

**Independent Test**：不註冊不登入，直接用 curl/.http 呼叫兩個 GET，能取得資料且分頁規則生效。

### Implementation（DB SP → Backend API → Frontend 顯示 → 驗收腳本）

- [x] T021 [US1] 新增貼文列表 SP（新到舊、排除 deleted；支援 limit/offset 且回 total）於 `DB/200_sp_post.sql`
- [x] T022 [US1] 新增留言列表 SP（若貼文 deleted 則 404；支援 limit/offset 且回 total；排序固定一致）於 `DB/210_sp_comment.sql`
- [x] T023 [P] [US1] 建立貼文 DAO（呼叫 sp_post_list）於 `backend/src/main/java/com/example/platform/dao/PostDao.java`
- [x] T024 [P] [US1] 建立留言 DAO（呼叫 sp_comment_list_by_post）於 `backend/src/main/java/com/example/platform/dao/CommentDao.java`
- [x] T025 [US1] 實作 GET /posts（含 limit/offset 驗證；回 PostListResponse）於 `backend/src/main/java/com/example/platform/api/PostController.java`
- [x] T026 [US1] 實作 GET /posts/{postId}/comments（deleted post → 404）於 `backend/src/main/java/com/example/platform/api/CommentController.java`
- [ ] T027 [P] [US1] 建立前端貼文列表頁（呼叫 GET /api/posts；顯示作者/內容/時間）於 `frontend/src/pages/PostsPage.vue`
- [ ] T028 [P] [US1] 建立前端貼文詳情頁（顯示留言列表；支持分頁）於 `frontend/src/pages/PostDetailPage.vue`
- [x] T029 [US1] 建立 US1 可重跑驗收腳本（.http 或 curl；含 limit/offset 範例與 400 範例）於 `docs/us1-acceptance.http`

**Checkpoint**：US1 腳本可重跑且通過；貼文列表不含已軟刪除貼文。

---

## Phase 4: User Story 2 - 使用手機號碼註冊與登入 (Priority: P1)

**Goal**：完成註冊/登入/refresh rotation 與 `/me`；login/register 有最小 rate limit（429）。

**Independent Test**：以 E.164 手機註冊→登入→拿 access token 呼叫 `/me`；用 refresh 換發並確認舊 refresh 失效。

### Implementation（DB SP → Backend Auth → Frontend 表單 → 驗收腳本）

- [x] T030 [US2] 新增使用者註冊 SP（phone_e164 unique；回 UserProfile）於 `DB/100_sp_user.sql`
- [x] T031 [US2] 新增使用者查詢 SP（登入用：查 hash/salt；phone normalize 後查）於 `DB/100_sp_user.sql`
- [x] T032 [US2] 新增個人資料 SP（/me）於 `DB/100_sp_user.sql`
- [x] T033 [US2] 新增 refresh token 簽發/驗證/撤銷/rotation SP（原子性更新舊 token + 插入新 token）於 `DB/110_sp_refresh_token.sql`
- [x] T034 [P] [US2] 實作密碼 salt+hash（建議 PBKDF2 或 BCrypt；提供 verify）於 `backend/src/main/java/com/example/platform/security/PasswordHasher.java`
- [x] T035 [P] [US2] 實作 JWT service（issue/verify；expiresInSeconds；UTC time）於 `backend/src/main/java/com/example/platform/security/JwtService.java`
- [x] T036 [P] [US2] 實作 refresh token 產生/雜湊/比對（DB 只存 token_hash）於 `backend/src/main/java/com/example/platform/security/RefreshTokenService.java`
- [x] T037 [US2] 實作 auth middleware（解析 Authorization: Bearer；建立 principal；未登入回 401）於 `backend/src/main/java/com/example/platform/security/JwtAuthFilter.java`
- [x] T066 [P] [US2] 補齊 Swagger/OpenAPI BearerAuth 設定（Swagger UI 右上角 Authorize 可輸入 JWT；/me 與其他受保護 API 顯示鎖頭；Try it out 會帶 Authorization: Bearer <token>）於 `backend/src/main/java/com/example/platform/config/OpenApiConfig.java`
- [x] T038 [US2] 實作最小 rate limit（login/register；超限 429；訊息一般化）於 `backend/src/main/java/com/example/platform/security/RateLimitFilter.java`
- [x] T039 [US2] 實作 POST /auth/register（E.164 normalize；coverImage URL<=2048；409/400/429）於 `backend/src/main/java/com/example/platform/api/AuthController.java`
- [x] T040 [US2] 實作 POST /auth/login（回 accessToken+refreshToken+user；401/429）於 `backend/src/main/java/com/example/platform/api/AuthController.java`
- [x] T041 [US2] 實作 POST /auth/refresh（rotation：舊 refresh 立刻失效；401）於 `backend/src/main/java/com/example/platform/api/AuthController.java`
- [x] T042 [US2] 實作 GET /me（需登入；回 UserProfile）於 `backend/src/main/java/com/example/platform/api/MeController.java`
- [ ] T043 [P] [US2] 建立前端註冊/登入頁（呼叫 /api/auth/register,/api/auth/login；保存 token）於 `frontend/src/pages/AuthPage.vue`
- [ ] T044 [US2] 建立前端 token 儲存與自動 refresh（最小：401 時觸發 refresh 再重試一次）於 `frontend/src/api/auth.ts`
- [x] T045 [US2] 建立 US2 可重跑驗收腳本（含：重複註冊 409、錯誤密碼 401、refresh 舊 token 失效）於 `docs/us2-acceptance.http`

**Checkpoint**：US2 腳本可重跑且通過；對外錯誤不含 stack/SQL。

---

## Phase 5: User Story 3 - 已登入使用者可發文並管理自己的貼文 (Priority: P2)

**Goal**：登入者可新增貼文；作者可 PATCH/DELETE 自己的貼文；非作者 403；軟刪除後列表不顯示。

**Independent Test**：兩帳號：A 建貼文 → B 嘗試改/刪（403）→ A 可改/刪；刪後 GET /posts 不出現。

### Implementation

- [x] T046 [US3] 新增建立貼文 SP（回 Post；image URL<=2048）於 `DB/200_sp_post.sql`
	- SP 名稱：`sp_post_create`
	- Params（named）：`p_author_user_id (uuid)`, `p_content (text)`, `p_image_url (varchar(2048), nullable)`
	- 規則：`p_content` 必填且不得全空白；`p_image_url` 若提供，需 http/https 且長度 <= 2048（不收 Base64）
	- Return（1 row）：`post_id, author_user_id, author_user_name, author_cover_image_url, content, image_url, created_at, updated_at`

- [x] T047 [US3] 新增更新貼文 SP（作者檢查；非作者對應 403）於 `DB/200_sp_post.sql`
	- SP 名稱：`sp_post_update`
	- Params（named）：`p_actor_user_id (uuid)`, `p_post_id (uuid)`, `p_content (text, nullable)`, `p_image_url (varchar(2048), nullable)`
	- 規則：
		- 必須先檢查貼文存在且未被軟刪除（`deleted_at IS NULL`）；不存在或已刪除 → 視為 not found
		- 作者檢查：`posts.author_user_id = p_actor_user_id`，否則 → forbidden
		- 更新欄位：允許部分更新；但不得讓更新後 `content` 變成全空白
		- `p_image_url` 規則同 T046（http/https、<=2048）
	- Return（1 row + meta）：
		- `post_id, author_user_id, author_user_name, author_cover_image_url, content, image_url, created_at, updated_at`
		- `post_exists (boolean)`, `post_deleted (boolean)`, `is_author (boolean)`
	- Backend 映射規則（需一致）：
		- `post_exists=false` 或 `post_deleted=true` → API 回 404
		- `is_author=false` → API 回 403
		- 其他 → 回 200 + Post

- [x] T048 [US3] 新增軟刪除貼文 SP（作者檢查；deleted_at 設定）於 `DB/200_sp_post.sql`
	- SP 名稱：`sp_post_soft_delete`
	- Params（named）：`p_actor_user_id (uuid)`, `p_post_id (uuid)`
	- 規則：
		- 若貼文不存在或已刪除（`deleted_at IS NOT NULL`）→ not found（對外 404；不做重複刪除）
		- 若存在但非作者 → forbidden（對外 403）
		- 若為作者且未刪除 → 設定 `deleted_at = now()`、更新 `updated_at`
	- Return（1 row + meta）：`post_exists (boolean)`, `post_deleted (boolean)`, `is_author (boolean)`

- [x] T049 [US3] 實作 POST /posts（需登入；回 201）於 `backend/src/main/java/com/example/platform/api/PostController.java`
	- Request/Response 以 OpenAPI 為準：`CreatePostRequest` → `Post`
	- 驗證：`content` 必填且不得全空白；`image` 若提供需 http/https 且長度 <= 2048
	- 授權來源：從 JWT Bearer 建立的 principal 取得 `userId`（作者）並傳入 `sp_post_create`

- [x] T050 [US3] 實作 PATCH /posts/{postId}（需登入且作者；403/404）於 `backend/src/main/java/com/example/platform/api/PostController.java`
	- Request/Response 以 OpenAPI 為準：`UpdatePostRequest` → `Post`
	- 驗證：`content` 若提供不得全空白；`image` 若提供需 http/https 且長度 <= 2048
	- 403/404 判斷必須依 `sp_post_update` 回傳 meta（不得自行 SQL 查表）

- [x] T051 [US3] 實作 DELETE /posts/{postId}（需登入且作者；軟刪除；204/403/404）於 `backend/src/main/java/com/example/platform/api/PostController.java`
	- 行為：成功軟刪除回 204
	- 重複呼叫：若貼文不存在或已刪除 → 回 404（不做重複刪除）
	- 403/404 判斷必須依 `sp_post_soft_delete` 回傳 meta（不得自行 SQL 查表）
- [ ] T052 [P] [US3] 建立前端發文 UI（表單 + 呼叫 POST /api/posts）於 `frontend/src/components/CreatePostForm.vue`
- [ ] T053 [P] [US3] 建立前端貼文管理 UI（作者可見 edit/delete）於 `frontend/src/components/PostActions.vue`
- [x] T054 [US3] 建立 US3 可重跑驗收腳本（兩帳號驗證 403；刪後列表不顯示）於 `docs/us3-acceptance.http`

**Checkpoint**：US3 腳本可重跑且通過。

---

## Phase 6: User Story 4 - 已登入使用者可對貼文留言，所有人可看留言列表 (Priority: P2)

**Goal**：登入者可新增留言；訪客可看留言列表；貼文軟刪除後留言列表/新增留言一律 404。

**Independent Test**：對未刪除貼文新增留言成功；刪除貼文後，再 GET/POST comments 皆 404。

### Implementation

- [ ] T055 [US4] 新增建立留言 SP（若貼文 deleted 則 404；回 Comment）於 `DB/210_sp_comment.sql`
- [ ] T056 [US4] 實作 POST /posts/{postId}/comments（需登入；deleted post → 404）於 `backend/src/main/java/com/example/platform/api/CommentController.java`
- [ ] T057 [P] [US4] 建立前端新增留言 UI（登入可輸入送出；成功後刷新列表）於 `frontend/src/components/CreateCommentForm.vue`
- [ ] T058 [US4] 建立 US4 可重跑驗收腳本（含：未登入 401、deleted post 404）於 `docs/us4-acceptance.http`

**Checkpoint**：US4 腳本可重跑且通過。

---

## Phase 7: Polish & Cross-Cutting Concerns（跨故事收尾）

- [ ] T059 強化 OpenAPI 契約一致性（比對 `contracts/openapi.yaml` 與實作；補齊缺漏的 schema/response）於 `specs/001-social-platform/contracts/openapi.yaml`
- [ ] T060 強化安全：確保所有輸入輸出不會造成 XSS（前端禁止 v-html；必要時做輸出編碼）於 `frontend/src/components/PostContent.vue`
- [ ] T061 確認後端不洩漏 stack trace/SQL（包含 DataAccessException/Unhandled exception）於 `backend/src/main/java/com/example/platform/common/GlobalExceptionHandler.java`
- [ ] T062 Quickstart 驗證與更新（確保路徑/指令與實際一致）於 `specs/001-social-platform/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup（Phase 1）→ Foundational（Phase 2）→ User Stories（Phase 3+）→ Polish（Phase 7）

### User Story Dependencies (recommended)

- US1（P1）：依賴 Phase 2；不依賴登入
- US2（P1）：依賴 Phase 2；提供後續受保護 API 基礎
- US3（P2）：依賴 US2（需要登入與 author principal）
- US4（P2）：依賴 US2（需要登入），並與 US1（留言列表）互補

---

## Parallel Execution Examples

### Setup / Foundational

- 可並行：T003（nginx/default.conf）、T006（backend/pom.xml）、T007（frontend/package.json）、T005（biome.json）

### US1

- 可並行：T023（PostDao）與 T024（CommentDao）；T027（PostsPage.vue）與 T028（PostDetailPage.vue）

### US2

- 可並行：T034（PasswordHasher）與 T035（JwtService）與 T038（RateLimitFilter）

### US3

- 可並行：T052（CreatePostForm.vue）與 T053（PostActions.vue）

### US4

- 可並行：T057（CreateCommentForm.vue）可在後端 API 完成後接上

---

## Implementation Strategy

### MVP Scope（建議先交付 US1）

1. 完成 Phase 1 + Phase 2（Compose/Swagger/DB schema/seed/後端基礎）
2. 完成 US1（只做瀏覽：貼文列表 + 留言列表）
3. 用 `docs/us1-acceptance.http` 重跑驗收

### Incremental Delivery

- US1（瀏覽）→ US2（登入/refresh）→ US3（發文/管理）→ US4（留言新增）→ Polish

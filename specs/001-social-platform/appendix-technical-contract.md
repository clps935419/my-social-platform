# 技術附錄：介面契約 / 安全 / 部署 / SP-first

**Feature**: [spec.md](spec.md)
**Created**: 2026-01-30

本檔案是為了滿足「既定系統架構/技術限制」與「可驗收介面契約」而提供的技術附錄；產品規格本體請以 [spec.md](spec.md) 為準。

## 既定架構與硬性限制（需驗收）

- 三層式架構：Nginx Web Server + Spring Boot Application Server + PostgreSQL Database。
- Web Server 指定 Nginx：提供前端靜態檔案並反向代理後端 API。
- 部署方式指定 Docker Compose：可一鍵啟動 nginx/app/db；Nginx 需反代 `/api/*` 至後端。
- 前端：Vue 3 + Vite + TypeScript（前端一律使用 TS）。
- 後端：Spring Boot、RESTful API。
- 建置：Maven。
- 資料存取：SP-first，所有 DB 操作必須透過 Stored Procedure。
  - 後端呼叫 SP 優先使用 JdbcTemplate 或 SimpleJdbcCall。
  - 禁止 ORM/JPA Repository 直接 CRUD 表。
  - 禁止任何 SQL 字串拼接（dynamic SQL / concatenation）。
- 交易：異動多個資料表時必須使用 Transaction（任一步驟失敗需回滾）。
- DB 腳本：DDL/DML/Stored Procedure 腳本必須放在專案根目錄 `DB/`。
- API 文件：必須提供 OpenAPI 3 與 Swagger UI（Docker Compose 啟動後也可存取且路徑固定並寫入文件）。
  - 固定路徑（經 nginx 對外）：`/api/swagger-ui/index.html`

## 前端實作建議（非驗收 / 供面試題快速落地）

以下是「能在三天內交付、又不會過度設計」的前端建議做法；後端 API 契約不因前端選型而變動。

- UI 元件框架（推薦）：Element Plus（Vue 3 主流、上手快、適合 CRUD/表單/列表）。
  - 可接受替代：Vuetify（Material Design）、Naive UI（較輕量、風格現代）。
- 資料抓取/快取：TanStack Query（處理 server state、快取、重試、載入狀態）。
- 狀態管理（是否需要）：
  - MVP 通常不必強制導入全域狀態管理；多數狀態可由 TanStack Query + local component state 解決。
  - 若需要集中管理「登入者資訊、token 狀態、全域 UI（例如 toast）」：推薦 Pinia（Vue 3 主流）。
- TypeScript（強制）：
  - 前端檔案一律使用 TypeScript（`.ts`、`.vue` 內 `<script lang="ts">`）。
  - 設定路徑別名：`@` → `frontend/src/`。
  - 匯入規則：優先使用 `@/` 絕對路徑；避免跨層相對路徑（例如 `../../`）。
- Lint / Format：Vue 本身不內建 lint；MVP 優先採用 Biome（針對 TS/JSON 等做 format + lint）。
  - 若要補齊 `.vue` template 的規則檢查，可加上 ESLint + eslint-plugin-vue（選配）。
- 測試（最小 POC）：以「可重跑的驗收腳本」或「少量 smoke 測試」為主；完整策略請依專案憲法的品質門檻。

## 權限規則（驗收準則）

- 訪客（未登入）：可瀏覽貼文/留言；不可發文、不可留言、不可編輯/刪除。
- 一般使用者（已登入）：可新增貼文；可對任一貼文留言；僅作者可編輯/刪除自己的貼文。
- 未登入呼叫受保護 API：必須被拒絕（401）。
- 已登入但無權限（例如非作者編輯/刪除）：必須回應 403。

## API 草案（路由與 request/response 欄位）

### 通用約定

- Base path：`/api`
- 授權：採 `Authorization: Bearer <accessToken>`（若改用 Session Cookie，需同步調整此附錄與後端行為）
- 時間欄位：UTC、ISO 8601（例如 `2026-01-30T12:34:56Z`）
- 圖片欄位：`Post.image`、`User.coverImage` 為 URL 字串（僅允許 http/https），最大長度 2048；不做檔案上傳、不收 Base64。
- 錯誤回應格式（範例）：`{"errorCode":"...","message":"..."}`

### Auth

- `POST /api/auth/register`（公開）
  - Request：`{ phoneNumber, userName, email?, password, coverImage?, biography? }`
  - Response 201：`{ userId, phoneNumber, userName, email?, coverImage?, biography?, createdAt }`
  - 規則：手機號碼採 E.164；允許輸入含空白/破折號/括號，後端正規化後判斷唯一性。
  - Errors：400（格式/必填）、409（手機號碼已存在）、429（rate limit）

- `POST /api/auth/login`（公開）
  - Request：`{ phoneNumber, password }`
  - Response 200：`{ accessToken, refreshToken, tokenType, expiresInSeconds, user: { userId, phoneNumber, userName, coverImage?, biography? } }`
  - 規則：手機號碼採 E.164；允許輸入含空白/破折號/括號，後端正規化後驗證。
  - Errors：400（必填/格式）、401（憑證錯誤）、429（rate limit）

- `POST /api/auth/refresh`（公開）
  - 說明：refresh token 採 rotation；每次 refresh MUST 換發新的 refresh token，且舊 token MUST 立即失效。
  - 安全：refresh token 建議只在 DB 儲存雜湊值（避免 DB 外洩時 token 可被直接重放）。
  - Request：`{ refreshToken }`
  - Response 200：`{ accessToken, refreshToken, tokenType, expiresInSeconds }`
  - Errors：400（必填/格式）、401（refresh token 無效或過期）

- `GET /api/me`（需登入）
  - Response 200：`{ userId, phoneNumber, userName, email?, coverImage?, biography?, createdAt, updatedAt? }`
  - Errors：401

### Posts

- `GET /api/posts`（公開）
  - Query（可選）：`limit?`, `offset?`
  - Response 200：`{ items: [ { postId, author:{ userId, userName, coverImage? }, content, image?, createdAt, updatedAt? } ], total: number }`
  - 規則：排序新到舊；排除軟刪除；`offset` 預設 0；`limit` 預設 20、上限 100；超限回 400。

- `POST /api/posts`（需登入）
  - Request：`{ content, image? }`
  - Response 201：`{ postId, author:{ userId, userName }, content, image?, createdAt }`
  - Errors：400、401

- `PATCH /api/posts/{postId}`（需登入且作者）
  - Request：`{ content?, image? }`
  - Response 200：`{ postId, content, image?, createdAt, updatedAt }`
  - Errors：400、401、403、404

- `DELETE /api/posts/{postId}`（需登入且作者；軟刪除）
  - Response 204
  - Errors：401、403、404

### Comments

- `GET /api/posts/{postId}/comments`（公開）
  - Query（可選）：`limit?`, `offset?`
  - Response 200：`{ items: [ { commentId, postId, author:{ userId, userName, coverImage? }, content, createdAt } ], total: number }`
  - 規則：排序固定一致（預設由舊到新）；`offset` 預設 0；`limit` 預設 20、上限 100；超限回 400。
  - 規則：若貼文已軟刪除，留言列表與新增留言一律回 404。

- `POST /api/posts/{postId}/comments`（需登入）
  - Request：`{ content }`
  - Response 201：`{ commentId, postId, author:{ userId, userName }, content, createdAt }`
  - Errors：400、401、404

## Stored Procedure 覆蓋範圍清單（最低要求）

至少包含：

- 使用者
  - `sp_user_register`：註冊（含手機號碼唯一檢查）
  - `sp_user_get_by_phone`：登入用查詢（含密碼驗證所需資料）
  - `sp_user_get_profile`：取得個人資料（/me）

- Refresh Token（rotation）
  - `sp_refresh_token_issue`：登入成功後簽發 refresh token（建議儲存雜湊值）
  - `sp_refresh_token_rotate`：refresh 時輪替（建立新 token、使舊 token 失效）
  - `sp_refresh_token_validate`：驗證 refresh token 是否有效/未撤銷/未過期
  - `sp_refresh_token_revoke`：撤銷 refresh token（用於登出或風控）

- 貼文
  - `sp_post_create`：新增貼文
  - `sp_post_list`：列出貼文（新到舊、排除軟刪除）
  - `sp_post_update`：更新貼文（需驗證作者）
  - `sp_post_soft_delete`：軟刪除貼文（需驗證作者）

- 留言
  - `sp_comment_create`：新增留言
  - `sp_comment_list_by_post`：取得貼文留言列表

## 安全要求（驗收）

- SQL Injection：所有 DB 呼叫必須以參數化方式呼叫 SP；不得拼接 SQL 字串；不得動態 SQL。
- XSS：貼文/留言/簡介等使用者輸入在任何顯示介面不得被當作可執行腳本；需採一致策略（輸出編碼或白名單）。
- 密碼：必須 salt+hash 後儲存；不可明碼；不可在 API 回傳。
- 錯誤訊息：對外錯誤不得洩漏 SQL/堆疊/敏感資訊；以一般化錯誤碼與訊息回覆。

## 驗收條件（技術面）

- Docker Compose：`docker compose up -d` 可一鍵啟動 nginx/app/db。
- Nginx 反代：`/api/*` 正確轉送後端；靜態站點可被存取。
- Swagger：啟動後可透過固定路徑打開 Swagger UI，並可試呼叫 API。
- DB/：專案根目錄 `DB/` 內可找到 DDL/DML/SP 腳本。
- SP-first：後端無直接 CRUD 表的 ORM/JPA Repository；無 SQL 字串拼接。
- Transaction：涉及多表異動的流程，任一步驟失敗時回滾，無部分成功狀態。

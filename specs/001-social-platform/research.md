# Research (Phase 0)

**Feature**: [spec.md](spec.md)  
**Branch**: `001-social-platform`  
**Date**: 2026-01-30

本檔案用來把原本在規格中可能出現的「NEEDS CLARIFICATION」全部收斂成可實作的決策與理由，確保進入設計/契約階段時不會卡住。

## Decisions

### Auth：JWT + Refresh Token（Rotation）
- Decision：採 `Authorization: Bearer <accessToken>`；refresh token 採 **rotation**（每次 refresh 都換發新的 refresh token，舊的立即失效）。
- Rationale：符合面試題「安全性與可撤銷」的最小可行方案；rotation 對 token 外洩的風險更友善。
- Alternatives considered：
  - Non-rotating（較省事但風險較高）
  - Rotation + reuse detection（更安全但複雜度高，非 MVP）

### Refresh token 存放策略
- Decision：refresh token **存 DB**（建議只存雜湊值）；access token 驗證 **無狀態**（不存 DB），且不做 JWT 黑名單。
- Rationale：refresh token 需要可撤銷/可失效（例如使用者登出或風控）；access token 保持無狀態以降低耦合。
- Alternatives considered：JWT 黑名單（需要額外儲存與查詢、增加複雜度）。

### 圖片欄位
- Decision：`Post.image`、`User.coverImage` 為 URL 字串（僅允許 http/https），最大長度 2048；不做檔案上傳、不收 Base64。
- Rationale：三天 MVP 最小可行、前後端契約簡單、DB 壓力小。
- Alternatives considered：檔案上傳（multipart + 物件儲存）、Base64（資料膨脹）。

### 分頁
- Decision：採 `limit/offset`；`offset` 預設 0；`limit` 預設 20、上限 100；不合法回 400。
- Rationale：最容易實作與驗收，符合面試題。
- Alternatives considered：cursor pagination（更佳但需要更多設計）。

### 時區
- Decision：後端時間一律 UTC 儲存與輸出（ISO 8601、含 `Z`）。
- Rationale：跨時區一致性、容易驗收。
- Alternatives considered：依使用者時區輸出（需求不足，非 MVP）。

### 軟刪除貼文與留言行為
- Decision：貼文軟刪除後，留言列表與新增留言一律回 404。
- Rationale：避免「刪除貼文但仍可互動」造成困惑；行為一致好驗收。
- Alternatives considered：仍可讀留言/仍可新增（規則更複雜）。

### 手機號碼
- Decision：採 E.164；允許輸入含空白/破折號/括號，後端正規化後判斷唯一性；格式不符回 400。
- Rationale：輸入彈性 + 資料庫一致性。

### 風控
- Decision：對 login（可含 register）做最小 rate limit；超過門檻回 429；錯誤訊息一般化避免枚舉。
- Rationale：用最小成本降低爆破風險。
- Alternatives considered：帳號鎖定/黑名單/圖形驗證（非 MVP）。

## Technology Choices (MVP-friendly)

- Backend：Java 17 + Spring Boot 3.x + springdoc-openapi（Swagger UI）
- DB：PostgreSQL 16
- DB access：JdbcTemplate/SimpleJdbcCall（SP-first）
- Frontend：Vue 3 + Vite
- UI：Element Plus（主流、上手快）
- Server state：TanStack Query
- Client state：預設不導入 Pinia；需要時再加（選配）
- Lint/format：Biome（ESLint for .vue template 規則為選配）

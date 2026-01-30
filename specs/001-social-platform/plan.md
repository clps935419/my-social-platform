# Implementation Plan: 簡易社群媒體平台

**Branch**: `001-social-platform` | **Date**: 2026-01-30 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-social-platform/spec.md`

## Summary

本計畫將交付一個可用 Docker Compose 一鍵啟動的三層式簡易社群平台：Nginx（前端靜態檔 + `/api/*` 反代）+ Spring Boot REST API + PostgreSQL。

功能範圍（依 spec 的 User Stories）：
- 訪客可瀏覽貼文與留言
- 手機號碼註冊/登入（JWT Bearer）
- 貼文：新增/列表/編輯/軟刪除（作者限定）
- 留言：新增/列表（貼文軟刪除後，留言讀寫一律 404）
- Auth：access token 無狀態；refresh token 採 rotation 且可由 DB 撤銷

關鍵技術策略：
- DB 存取嚴格採 SP-first：所有 DB 操作皆透過 Stored Procedure，後端以 JdbcTemplate/SimpleJdbcCall 參數化呼叫
- 安全底線：密碼 salt+hash；錯誤不洩漏 SQL/stack trace；避免 XSS（前端不做不安全渲染）

## Technical Context

**Language**: Java 17（backend）、TypeScript/JavaScript（frontend）

**Language/Version**:
- Backend：Java 17 + Spring Boot 3.x
- Frontend：Node.js 20 + Vue 3 + Vite

**Primary Dependencies**:
- Backend：spring-boot-starter-web、spring-boot-starter-validation、spring-boot-starter-jdbc、springdoc-openapi（Swagger UI）
- Frontend：Vue 3 + Vite、TanStack Query（server state；採 @tanstack/vue-query）、Axios、Vue Router（若需要頁面路由）

UI 框架：Element Plus。

**Storage**: PostgreSQL 16（Stored Procedures）

**Testing**:
- 依憲法：最小 POC 以可重跑 API 驗收腳本（curl/.http）為主；少量 smoke tests（選配）

**Target Platform**: Docker Compose（nginx/app/db）

**Project Type**: web（frontend + backend）

**Performance Goals**: MVP 不做壓測；以一般操作順暢為目標

**Constraints**:
- SP-first + 禁止 SQL 字串拼接 + 禁止 ORM 直接 CRUD
- Nginx 反代 `/api/*`
- Swagger UI Compose 啟動後可存取且路徑固定（預設 `/api/swagger-ui/index.html`）
- 時間欄位 UTC、ISO 8601（含 `Z`）

**Scale/Scope**: 面試題三天 MVP（優先可驗收、避免過度設計）

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Gates（計畫中必須逐項明確確認）：
- 技術棧：Vue 3 + Vite、Spring Boot REST、Maven
- 資料庫：PostgreSQL
- 資料存取：僅允許 Stored Procedure（SP-first），優先用 JdbcTemplate/SimpleJdbcCall 呼叫；禁止 ORM 直接 CRUD；禁止 SQL 字串拼接
- 部署：Docker Compose（nginx/app/db），且 Nginx 需反代 `/api/*`
- API 文件：OpenAPI 3 + Swagger UI（Compose 啟動後可存取）
- DB 腳本：DDL/DML/SP 腳本放在 `DB/`
- 安全底線：防 SQLi（參數化）、防 XSS（禁止不安全渲染）、密碼 salt+hash、錯誤不洩漏敏感資訊

## Project Structure

### Documentation (this feature)

```text
specs/001-social-platform/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── appendix-technical-contract.md
├── contracts/
│   └── openapi.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
DB/
├── 001_schema.sql
├── 010_tables.sql
├── 020_indexes.sql
├── 100_sp_user.sql
├── 110_sp_refresh_token.sql
├── 200_sp_post.sql
└── 210_sp_comment.sql

nginx/
└── default.conf

docker-compose.yml

backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/platform/
│       │       ├── api/        # controllers + DTO
│       │       ├── service/    # use cases + authz + tx
│       │       ├── dao/        # stored procedure calls only
│       │       ├── security/   # jwt + password
│       │       └── common/     # error mapping
│       └── resources/
│           └── application.yml
└── pom.xml

frontend/
├── src/
│   ├── pages/
│   ├── components/
│   └── api/            # API client + TanStack Query hooks
└── package.json
```

**Structure Decision**: 採 web app 雙專案（`frontend/` + `backend/`）並在 repo root 放置 `docker-compose.yml`、`nginx/`、`DB/`。

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |

本計畫無需違反專案憲法（GATE: PASS）。

## Phase 0: Outline & Research (Output)

- 已產出 [research.md](research.md)：所有 clarifications 已定案並轉成可實作決策。

## Phase 1: Design & Contracts (Output)

- 已產出 [data-model.md](data-model.md)：entities/relationships/validation/state。
- 已產出 [contracts/openapi.yaml](contracts/openapi.yaml)：對外 API 契約草案。
- 已產出 [quickstart.md](quickstart.md)：最小可跑與最小驗收方式。

## Phase 2: Implementation Planning (High-level)

1. Compose + Nginx：一鍵啟動、`/api/*` 反代、Swagger UI 固定路徑
2. DB：建表 + index + SP（含 refresh rotation 的原子性）
3. Backend：Controller/Service/DAO 分層；DAO 僅呼叫 SP
4. Frontend：以 TanStack Query 串接契約；UI 用 Element Plus 快速落地
5. POC 驗收：補齊可重跑的 API 驗收腳本（符合憲法最小測試門檻）

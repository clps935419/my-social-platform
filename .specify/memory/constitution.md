<!--
Sync Impact Report

- Version change: 1.0.1 -> 1.1.0
- Modified principles: Added minimal testing + frontend tooling baselines
- Added sections: VI. 前端工具鏈底線（Lint/Format）、最小測試（POC）門檻
- Removed sections: None
- Templates requiring updates:
	- .specify/templates/plan-template.md: updated (Constitution Check gates)
	- .specify/templates/spec-template.md: no change
	- .specify/templates/tasks-template.md: no change
	- .specify/templates/checklist-template.md: no change
	- .specify/templates/commands/*.md: N/A (folder not present)
- Follow-up TODOs:
	- TODO(RATIFICATION_DATE): Set the original ratification date once agreed.
-->

# 簡易社群媒體平台專案憲法（Constitution）

## Core Principles

### I. 技術棧不可變（Tech Stack Non-negotiable）
- 前端必須使用 Vue 3 + Vite。
- 後端必須使用 Spring Boot，並以 RESTful API 提供服務。
- 建置工具必須使用 Maven。
- API 文件必須採用 OpenAPI 3，並提供 Swagger UI。
- Swagger UI 必須在 Docker Compose 啟動後可存取，且存取路徑必須固定並寫入文件。

### II. 可重現部署優先（Compose + Nginx Required）
- 系統必須可透過 Docker Compose 一鍵啟動。
- Docker Compose 必須至少包含三個服務：nginx（Web Server）、app（Spring Boot）、db（PostgreSQL）。
- Nginx 必須提供前端靜態檔案，並反向代理後端 API（例如 `/api/*` -> app）。

### III. SP-first 資料存取（PostgreSQL + Stored Procedure Only）
- 資料庫必須使用 PostgreSQL。
- 所有資料庫操作必須透過 Stored Procedure（SP-first）。
- 後端呼叫 Stored Procedure 應優先使用 JdbcTemplate 或 SimpleJdbcCall，以避免不小心出現直接 CRUD。
- 禁止事項：
	- 任何 ORM/JPA Repository 直接對資料表做 CRUD（例如對 table 進行 save/find/delete）。
	- 在應用程式端以字串拼接 SQL / 組裝 dynamic SQL。
- 所有 DDL/DML/Stored Procedure 腳本必須存放於專案根目錄的 `DB/` 資料夾。

### IV. 安全底線不可破（Security Baselines）
- SQL Injection 防護：
	- Stored Procedure 呼叫必須使用參數化（parameterized）。
	- 禁止使用任何 SQL 字串拼接。
- XSS 防護：
	- 會在 UI 呈現的使用者輸入欄位必須做長度/格式驗證，必要時進行內容清理（sanitize）。
	- 前端禁止用 `v-html`（或等價的不安全 raw HTML 渲染）直接渲染未信任的輸入。
- 密碼處理：
	- 密碼必須以 salt + hash 方式儲存；禁止明碼儲存。
- 錯誤處理：
	- 對外錯誤回應不得洩漏敏感資訊（SQL、堆疊、密鑰等）。

### V. 分層清楚與可驗收（Layered Architecture + Quality Gates）
- 後端必須維持清楚分層：
	- Presentation（Controllers、Request/Response DTO）
	- Business（Services/UseCases：承載規則、權限檢查、交易邊界）
	- Data（DAO：只負責 Stored Procedure 呼叫與資料映射）
	- Common（例外處理、統一回應格式、驗證、安全共用工具）
- 授權檢查必須由後端強制執行（不可只靠前端隱藏按鈕）。
- 多表異動必須具備交易一致性；任一步驟失敗必須回滾。

### VI. 前端工具鏈底線（Lint/Format Baselines）

- Vue 本身不內建 lint；為了在面試題時限內保持一致性，前端建議採用 Biome 作為 format + lint 的主要工具。
- 若需要針對 `.vue` template 規則做更完整檢查，允許加上 ESLint + eslint-plugin-vue（選配，不強制）。

## Additional Constraints

### Product Invariants
- 平台必須提供：註冊、登入/驗證、發文（新增/列表/編輯/刪除）、留言（至少新增）。
- 訪客（未登入）可瀏覽貼文/留言，但不可互動（不可新增/編輯/刪除貼文，不可留言）。
- 僅貼文作者可編輯/刪除自己的貼文；非作者操作必須拒絕（403）。

### Decisions (Already Ratified)
- 登入方式：手機號碼 + 密碼。
- 貼文刪除策略：軟刪除（Soft Delete）。
- 訪客權限：可瀏覽、不可互動（view-only）。

## Workflow & Quality Gates

- 所有由 Speckit 產出的 plan/spec/tasks 必須明確逐項確認符合本憲法。
- 文件必須包含本機啟動方式與 Docker Compose 啟動方式。
- OpenAPI/Swagger 文件必須與實作同步，禁止長期偏離。

### 最小測試（POC）門檻

- 本題不以高覆蓋率為目標；優先交付可驗收的端到端流程。
- 至少提供一種「可重跑」的驗收方式（擇一即可）：
	- API 驗收腳本（例如 curl 指令集或 .http 檔），涵蓋：register、login、refresh、posts list/create/update/delete、comments list/create。
	- 或少量自動化 smoke 測試（聚焦上述關鍵路徑）。

## Governance

- 本憲法優先於所有其他文件（spec/plan/tasks/implementation）。
- 修憲流程：
	- 以文字提出修改（修改內容/理由/影響/遷移與相容性說明）。
	- 更新本檔案與頂部的 Sync Impact Report。
	- 確保 templates 與開發指引同步（例如 plan 的 Constitution Check）。
- 版本規則（Semantic Versioning）：
	- MAJOR：移除/重定義原則，或造成治理規則不相容。
	- MINOR：新增原則/章節，或對約束做實質擴張。
	- PATCH：文字澄清、措辭修正、不影響語意的整理。
- 遵循要求：
	- 每份 plan 必須包含 Constitution Check。
	- 若需違反憲法，必須先明確寫出理由並取得同意後才能實作。

**Version**: 1.1.0 | **Ratified**: TODO(RATIFICATION_DATE): Set original adoption date. | **Last Amended**: 2026-01-30

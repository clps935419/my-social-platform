# 社群媒體系統需求文件（給 Spec Kit /speckit.specify 參考）

本文件將題目需求整理成可直接提供給 Spec Kit 的需求規格（偏 PRD/Requirements），用於後續產生技術方案與任務拆解。

---

## 直接貼到 `/speckit.specify` 的版本（建議）

> 請建立一個「簡易社群媒體平台」的需求規格，並產出清楚可驗收的功能與非功能性需求。
>
> 產品目標：提供使用者可用「手機號碼」註冊與登入，登入後可以發文、查看貼文、編輯/刪除自己的貼文，並可對貼文留言。
>
> 功能必須包含：
> 1) 註冊：使用手機號碼註冊帳號（手機號碼同時作為登入識別）。
> 2) 登入驗證：實作身份驗證，只有登入使用者可發文或留言。
> 3) 發文：新增貼文、列出所有貼文、編輯或刪除貼文。
> 4) 留言：可針對貼文新增留言。
>
> 系統架構要求：三層式架構（Web Server + Application Server + 任一關聯式資料庫）。後端需依需求設計展示層、業務層、資料層、共用層。
> - Web Server 指定使用 Nginx（提供前端靜態檔案並反向代理後端 API）。
> - 部署方式指定使用 Docker Compose（可一鍵啟動 Web Server / App Server / DB）。
>
> 技術要求：
> - 前端使用 Vue.js。
> - 後端使用 Spring Boot。
> - 後端服務使用 RESTful API。
> - 專案建置工具使用 Maven 或 Gradle。
> - 必須透過 Stored Procedure 存取資料庫。
> - 異動多個資料表時要使用 Transaction（避免資料錯亂）。
> - 資料庫 DDL/DML 必須放在專案根目錄下的 `DB/` 資料夾。
> - 必須防止 SQL Injection 與 XSS。
>
> 資料模型至少包含 User / Post / Comment（可擴充）。
> - User：User ID(PK)、User Name、Email、Password(需加鹽並雜湊後儲存)、Cover Image(可選)、Biography。
> - Post：Post ID(PK)、User ID(FK->User)、Content、Image(可選)、Created At。
> - Comment：Comment ID(PK)、User ID(FK)、Post ID(FK)、Content、Created At。
>
> 請同時補齊：
> - 權限規則（誰可以編輯/刪除、誰可以留言）
> - 主要 API 行為（必要路由與 request/response 欄位）
> - 驗收條件（Acceptance Criteria）
> - 安全要求（密碼、輸入驗證、輸出編碼/過濾、Token/Session）

---

## 完整需求規格（展開版）

### 1. 專案摘要
建立一個簡易社群媒體平台，支援使用者以手機號碼註冊/登入，並在登入狀態下發文與留言。後端採三層式架構並以 RESTful API 對前端 Vue.js 提供服務，資料庫為關聯式資料庫且所有資料存取需透過 Stored Procedure。部署方式使用 Docker Compose，Web Server 使用 Nginx 供應前端與反向代理後端 API。

### 2. 角色與使用情境
- 訪客（未登入）
  - 可查看貼文與留言（可瀏覽、不可互動）。
  - 可註冊、登入。
- 一般使用者（已登入）
  - 可新增貼文。
  - 可編輯/刪除「自己的」貼文。
  - 可對任一貼文留言（至少包含新增留言）。

### 3. 功能需求

#### 3.1 註冊
- 使用者以「手機號碼」註冊帳號。
- 手機號碼必須唯一（不可重複註冊）。
- 必填欄位：手機號碼、使用者名稱、密碼（Email 可視需求是否必填；本需求保留為可填）。
- 密碼儲存規則：必須加鹽（salt）並進行雜湊（hash）後儲存，不可儲存明碼。
- （可選但建議）手機號碼格式驗證（長度、國碼、僅數字等）。

登入方式已確認：手機號碼 + 密碼。

#### 3.2 登入與身份驗證
- 使用者以手機號碼登入。
- 系統需提供身份驗證機制，確保只有登入使用者可以：
  - 新增貼文
  - 編輯/刪除貼文
  - 新增留言
- （建議）使用 token-based（例如 JWT）或 server-side session；需明確定義：
  - 登入成功回傳 token
  - 前端以 `Authorization: Bearer <token>` 呼叫受保護 API
- 登出：使 token 失效或清除 session（做法依選型而定）。

#### 3.3 貼文（Post）
- 新增貼文
  - 必填：Content
  - 選填：Image（可先以 URL 或 base64，具體方式可在技術方案決定）
  - Created At 由系統寫入
- 列出所有貼文
  - 至少提供時間排序（預設新到舊）
  - （建議）支援分頁參數（page/size）
- 編輯貼文
  - 僅貼文作者可編輯
  - 可編輯 Content、Image
- 刪除貼文
  - 僅貼文作者可刪除
  - 刪除策略：軟刪除（Soft Delete），不可硬刪除。
  - 軟刪除後：一般貼文列表預設不顯示已刪除貼文（除非管理/維運用途另有需求）。

#### 3.4 留言（Comment）
- 新增留言
  - 僅登入使用者可留言
  - 必填：Post ID、Content
  - Created At 由系統寫入
- （建議）取得貼文留言列表
  - 雖題目僅要求「新增留言」，但實務上前端需顯示留言；若要最小可用，至少提供查詢 API。

### 4. 權限與規則
- 未登入：不可新增/編輯/刪除貼文；不可留言。
- 已登入：可新增貼文；可對貼文留言。
- 僅作者：可編輯/刪除自己的貼文。

### 5. 系統架構與分層（要求）
- 三層式架構：
  - Web Server：指定使用 Nginx（提供前端靜態檔案/反向代理）
  - Application Server（Spring Boot）
  - Relational Database（MySQL / PostgreSQL / SQL Server 皆可）
- 後端分層要求：
  - 展示層（Presentation）：Controller/DTO
  - 業務層（Business）：Service/UseCase
  - 資料層（Data）：Repository/DAO（只負責資料存取）
  - 共用層（Common）：Exception、Response 格式、Auth 共用、Util 等

### 6. 技術與實作限制（硬性）
- 前端：Vue.js。
- 後端：Spring Boot。
- API：RESTful 風格。
- 建置：Maven。
- 資料存取：必須透過 Stored Procedure（不可直接拼接 SQL）。
- 交易：同時異動多表時需使用 Transaction。
- DB 腳本：DDL、DML 必須放在專案根目錄 `DB/`。

#### 6.1 部署方式（硬性）
- 必須提供 `docker-compose.yml`（或等價的 compose 檔案），可一鍵啟動整個系統。
- 必須包含並清楚定義至少三個服務角色：
  - `nginx`：Web Server，提供前端靜態檔案並反向代理後端 API
  - `app`：Spring Boot Application Server
  - `db`：關聯式資料庫
- Nginx 需將 `/api`（或等價路徑）反向代理到後端服務。
- 專案需提供啟動方式說明（例如 `docker compose up -d`）與必要的環境變數清單。

### 7. 資料庫需求

#### 7.1 必要資料表（至少）
- User
  - User ID（PK）
  - User Name
  - Email
  - Password（salt+hash 後儲存；可拆欄位存 salt 與 hash）
  - Cover Image（可選）
  - Biography
  - （建議新增）Phone Number（唯一，作為登入識別）
  - （建議新增）Created At / Updated At
- Post
  - Post ID（PK）
  - User ID（FK -> User）
  - Content
  - Image（可選）
  - Created At
  - （建議新增）Updated At
- Comment
  - Comment ID（PK）
  - User ID（FK -> User）
  - Post ID（FK -> Post）
  - Content
  - Created At

> 題目未列出 User 的「Phone」欄位，但因需求指定「以手機號碼註冊與登入」，因此資料表需能保存手機號碼（建議新增欄位並設 unique index）。

#### 7.2 Stored Procedure（要求）
至少需要覆蓋下列資料操作（可依實作調整）：
- User
  - 建立使用者（檢查 phone 唯一）
  - 依 phone 取得使用者（登入驗證用）
- Post
  - 建立貼文
  - 查詢貼文列表（支援分頁/排序）
  - 更新貼文（需驗證作者）
  - 刪除貼文（需驗證作者，並處理留言）
- Comment
  - 建立留言
  - 依 Post ID 查詢留言列表

#### 7.3 Transaction（要求）
- 任何需要同時異動多表的行為必須在同一個交易中完成，例如：
  - 刪除貼文同時刪除/處理相關留言
  - 註冊時同時寫入使用者主檔與初始化資料（若有）

### 8. 安全性需求（硬性）

#### 8.1 SQL Injection 防護
- 所有 DB 操作必須透過 Stored Procedure。
- Stored Procedure 與呼叫端均需採參數化傳遞，不可拼接 SQL。
- 所有輸入欄位需做基本格式與長度驗證（避免超長輸入與異常字元）。

#### 8.2 XSS 防護
- 對使用者可輸入並會被呈現在 UI 的欄位（Post.Content、Comment.Content、Biography 等）：
  - 後端需做輸入驗證與必要的內容清理（例如不允許 script tag）。
  - 前端顯示時需採安全輸出策略（避免使用 `v-html` 直接渲染未清理內容）。

#### 8.3 密碼安全
- 密碼不可明碼儲存。
- 需使用 salt + 強雜湊演算法（具體演算法由技術方案決定），並可設定合理的密碼長度/複雜度規則。

#### 8.4 認證與授權
- 受保護 API 必須驗證登入身份。
- 僅作者可改/刪貼文：後端需強制檢查（不可只靠前端隱藏按鈕）。

### 9. API 需求（建議路由草案）
以下為建議 RESTful 路由，供 `/speckit.plan` 再落地：
- Auth
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/logout`（可選）
  - `GET /api/auth/me`（可選）
- Posts
  - `POST /api/posts`（需登入）
  - `GET /api/posts`（可公開）
  - `PUT /api/posts/{postId}`（需登入且作者）
  - `DELETE /api/posts/{postId}`（需登入且作者）
- Comments
  - `POST /api/posts/{postId}/comments`（需登入）
  - `GET /api/posts/{postId}/comments`（建議）

### 10. 驗收條件（Acceptance Criteria）
- 註冊
  - 使用手機號碼可註冊成功。
  - 重複手機號碼註冊會失敗並回傳清楚錯誤訊息。
  - 密碼在資料庫中不可為明碼。
- 登入與權限
  - 未登入呼叫發文/留言 API 會被拒絕（401/403）。
  - 登入後可成功發文與留言。
- 貼文
  - 可新增貼文並在列表看到。
  - 可列出所有貼文（至少新到舊）。
  - 僅作者可以編輯/刪除自己的貼文；非作者操作會被拒絕（403）。
  - 刪除貼文採軟刪除：刪除後貼文不應出現在一般列表中。
- 留言
  - 登入後可對指定貼文新增留言。
  - （若提供列表）可在貼文下看到留言列表。
- DB 與 Stored Procedure
  - 專案根目錄存在 `DB/`，內含 DDL 與 DML。
  - 後端資料存取皆透過 Stored Procedure（不可出現拼字串 SQL）。
- 交易
  - 多表異動行為具交易一致性：任一步驟失敗需回滾，避免資料不一致。
- 安全
  - 對輸入欄位進行長度/格式驗證。
  - 基本 XSS 防護策略可驗證（例如輸入 `<script>` 不會在前端被執行）。

- 部署
  - 專案根目錄提供 Docker Compose 設定，可用 `docker compose up -d` 啟動。
  - Nginx 可提供前端靜態內容，並能將 API 請求反向代理到後端（例如 `/api/*`）。

---

## 已確認的決策
1) 登入方式：手機號碼 + 密碼。
2) 貼文刪除策略：軟刪除（Soft Delete）。
3) 訪客權限：可瀏覽貼文/留言，不可互動（不可發文、不可留言）。

## 待確認事項（建議在 `/speckit.clarify` 釐清）
以下項目不影響先產生規格，但會影響資料庫設計、API 以及前端行為，建議在進入 `/speckit.plan` 前先定案：

1) 資料庫選型：MySQL / PostgreSQL / SQL Server？（Stored Procedure 語法差異會影響 DB 腳本與 DAO 寫法）
2) 圖片欄位（Post.Image、User.Cover Image）型態：URL？base64？檔案上傳？若上傳，是否允許僅本機/容器內儲存？最大大小/格式限制？
3) Comment 功能範圍：只新增？是否需要「查詢留言列表」（前端顯示必備）、編輯/刪除留言？
4) User Profile 功能範圍：是否需要更新使用者名稱/Email/Biography/Cover Image？是否需要查看他人個人頁？
5) 帳號欄位規則：
  - Email 是否必填、是否需驗證格式與唯一？
  - 密碼長度/複雜度規則（最小長度、是否需要大小寫/數字/特殊字元）
6) Token/Session 策略：JWT 還是 Session？Token 有效期、刷新（refresh token）是否需要？
7) CORS/同網域：前端與後端是否同網域（經 Nginx 反代）？是否需要開放跨網域呼叫？
8) API 分頁與排序：貼文列表與留言列表是否需要分頁參數與預設排序？
9) 時區與時間格式：`Created At` 使用 UTC 還是本地時區？API 回傳格式（ISO 8601）？
10) 軟刪除細節：
  - 被軟刪除的貼文，留言是否仍可被查詢/顯示？
  - 是否提供管理/維運用途的「包含已刪除」查詢（需權限）？

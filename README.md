# Social Media Platform

簡易社群平台：手機號碼註冊/登入、貼文、留言

**Tech Stack**: Spring Boot 3 + Vue 3 + PostgreSQL 16 + Nginx

## 🚀 Quick Start (只需 Docker)

```bash
# 1. 複製環境變數
cp .env.example .env

# 2. 啟動所有服務
make up

# 3. 開啟瀏覽器
# Frontend: http://localhost/
# API Health: http://localhost/api/health
# Swagger UI: http://localhost:8080/api/swagger-ui/index.html
```

**首次啟動需等待 3-5 分鐘** (Maven 下載依賴)

## 📋 常用指令

```bash
make up          # 啟動服務
make down        # 停止服務
make logs        # 查看日誌
make ps          # 查看狀態
make health      # 健康檢查
make clean       # 清除所有資料重新開始
```

## 🏗️ 架構說明

- **Frontend**: Vue 3 + Vite + TypeScript + TanStack Query
- **Backend**: Spring Boot 3 (Java 17) + JdbcTemplate + Stored Procedures
- **Database**: PostgreSQL 16
- **Web Server**: Nginx (反向代理 + 靜態檔案)
- **Deployment**: Docker Compose

## 📁 專案結構

```
├── DB/                  # 資料庫初始化腳本 (schema, tables, indexes, seed)
├── backend/             # Spring Boot 後端 (REST API, DAO, Security)
├── frontend/            # Vue 3 前端 (pages, components, API client)
├── nginx/               # Nginx 設定
└── docker-compose.yml   # Docker Compose 設定
```

## 🔒 安全特性

- **SP-First**: 所有資料庫操作透過 Stored Procedures (防 SQL Injection)
- **JWT 認證**: 無狀態 access token + refresh token rotation
- **密碼加密**: Salt + Hash (PBKDF2/BCrypt)
- **Rate Limiting**: 防暴力破解
- **XSS 防護**: 前端安全渲染

## 🛠️ 開發模式

### 前端開發
```bash
cd frontend
npm install
npm run dev      # 開發伺服器 http://localhost:5173
npm run build    # 建置正式版
```

### 後端開發
```bash
cd backend
mvn spring-boot:run  # 需要 Java 17+
```

### 熱重載
- **Frontend**: 修改 `frontend/src/` 自動重新編譯
- **Backend**: 修改 `backend/src/` 自動重啟 (spring-boot-devtools)

## 📝 實作進度

**已完成**: Phase 1 & 2 (專案建置與基礎架構) ✅

詳細進度請參考 [IMPLEMENTATION_SUMMARY.md](./reports/IMPLEMENTATION_SUMMARY.md)

## 🔧 環境變數設定

編輯 `.env` 檔案 (預設值已可直接使用):
```bash
POSTGRES_DB=social_platform
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
JWT_SECRET=your-secret-here-min-32-chars
```

## 🐛 常見問題

### 服務無法啟動
```bash
make logs        # 查看錯誤日誌
make ps          # 檢查服務狀態
make clean       # 清除後重新啟動
make up
```

### 首次啟動很慢
正常現象，Maven 需下載依賴 (3-5 分鐘)
```bash
make logs -f app  # 監控後端啟動進度
```

### Port 衝突
編輯 `.env` 修改 port:
```bash
NGINX_PORT=8000
POSTGRES_PORT=5433
```

## 📚 詳細文件

- [Implementation Summary](./reports/IMPLEMENTATION_SUMMARY.md) - 實作報告
- [Deployment Instructions](./reports/DEPLOYMENT_INSTRUCTIONS.md) - 部署說明
- [API Contract](./specs/001-social-platform/contracts/openapi.yaml) - API 規格

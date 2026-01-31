# Social Media Platform

簡易社群平台：手機號碼註冊/登入、貼文、留言

**Tech Stack**: Spring Boot 3 + Vue 3 + PostgreSQL 16 + Nginx

## 🚀 Quick Start (需安裝 Docker)

```bash
# 1. 複製環境變數
cp .env.example .env

# 2. 啟動正式環境
make production

# 3. 開啟瀏覽器
# Frontend: http://localhost/
# API Health: http://localhost/api/health
# Swagger UI: http://localhost:8080/api/swagger-ui/index.html
```


## 📋 常用指令

```bash
make dev             # 啟動服務 (需要時自動 build)
make logs            # 查看日誌
make production      # 只用 Docker 的正式環境部署
make production-down # 停止正式環境
make volumes-reset   # 清除 volume 並重建
make clean           # 移除容器
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

### 本機用 Docker 開發
```bash
make dev
```

## 🔧 環境變數設定

編輯 `.env` 檔案 (預設值已可直接使用):
```bash
POSTGRES_DB=social_platform
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
JWT_SECRET=your-secret-here-min-32-chars
```

## 📚 詳細文件

- [Implementation Summary](./reports/IMPLEMENTATION_SUMMARY.md) - 實作報告
- [Deployment Instructions](./reports/DEPLOYMENT_INSTRUCTIONS.md) - 部署說明
- [API Contract](./specs/001-social-platform/contracts/openapi.yaml) - API 規格

# Quickstart (Phase 1)

**Feature**: [spec.md](spec.md)  
**Branch**: `001-social-platform`  
**Date**: 2026-01-30

## Prerequisites

- Docker Desktop（支援 `docker compose`）

## Run (Docker Compose)

### Option 1: Using Docker Compose directly

1. 啟動全部服務：
   - `docker compose up -d --build`
2. 驗證服務：
   - 前端（Nginx 靜態站）：`http://localhost/`
   - API base：`http://localhost/api`
   - Swagger UI（固定路徑）：`http://localhost/api/swagger-ui/index.html`
   - Health（後端存活檢查）：`http://localhost/api/health`

### Option 2: Using Makefile (recommended)

專案提供 Makefile 便捷指令：

- `make help` - 顯示所有可用指令
- `make dev` - 建置並啟動所有服務（等同 `docker compose up -d --build`）
- `make up` - 啟動服務（不建置）
- `make down` - 停止所有服務
- `make logs` - 查看服務日誌（追蹤模式）
- `make ps` - 查看服務狀態
- `make clean` - 清理所有容器、網路和卷
- `make rebuild` - 完全重建（清理後重新建置並啟動）
- `make health` - 檢查所有服務健康狀態

啟動範例：
```bash
make dev
```

驗證服務：
- 前端（Nginx 靜態站）：`http://localhost/`
- API base：`http://localhost/api`
- Swagger UI（固定路徑）：`http://localhost/api/swagger-ui/index.html`
- Health（後端存活檢查）：`http://localhost/api/health`

## Minimal POC Verification

依專案憲法的最小 POC 門檻，提供一份「可重跑」的 API 驗收腳本（建議放在 `docs/` 或以 `.http` 檔提供），至少涵蓋：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/posts` / `POST /api/posts`
- `PATCH /api/posts/{postId}` / `DELETE /api/posts/{postId}`
- `GET /api/posts/{postId}/comments` / `POST /api/posts/{postId}/comments`

驗收腳本位置：
- `docs/us1-acceptance.http` - 貼文與留言瀏覽測試
- `docs/us2-acceptance.http` - 註冊與登入測試
- `docs/us3-acceptance.http` - 貼文管理測試
- `docs/us4-acceptance.http` - 留言新增測試

## Notes

- DB 腳本（DDL/DML/SP）必須放在 `DB/`，並在 DB container 啟動時套用。
- 後端資料存取一律走 Stored Procedure（SP-first）。

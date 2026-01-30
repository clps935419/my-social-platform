# Quickstart (Phase 1)

**Feature**: [spec.md](spec.md)  
**Branch**: `001-social-platform`  
**Date**: 2026-01-30

## Prerequisites

- Docker Desktop（支援 `docker compose`）

## Run (Docker Compose)

1. 啟動全部服務：
   - `docker compose up -d --build`
2. 驗證服務：
   - 前端（Nginx 靜態站）：`http://localhost/`
   - API base：`http://localhost/api`
   - Swagger UI（固定路徑）：`http://localhost/api/swagger-ui/index.html`

## Minimal POC Verification

依專案憲法的最小 POC 門檻，提供一份「可重跑」的 API 驗收腳本（建議放在 `docs/` 或以 `.http` 檔提供），至少涵蓋：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/posts` / `POST /api/posts`
- `PATCH /api/posts/{postId}` / `DELETE /api/posts/{postId}`
- `GET /api/posts/{postId}/comments` / `POST /api/posts/{postId}/comments`

## Notes

- DB 腳本（DDL/DML/SP）必須放在 `DB/`，並在 DB container 啟動時套用。
- 後端資料存取一律走 Stored Procedure（SP-first）。

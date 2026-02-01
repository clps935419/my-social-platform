# Quick Start Guide - 快速開始指南

## 如何執行測試 (How to Run Tests)

### 方式 1: 使用 VS Code REST Client 擴充套件

1. 安裝 REST Client 擴充套件
   ```
   在 VS Code 擴充套件市場搜尋 "REST Client" by Huachao Mao
   ```

2. 開啟任何 `.http` 測試文件

3. 點擊每個測試上方的 "Send Request" 連結

4. 在右側面板查看回應結果

### 方式 2: 使用 IntelliJ IDEA HTTP Client

1. 開啟任何 `.http` 測試文件

2. 點擊每個請求旁的綠色播放按鈕 ▶️

3. 在底部面板查看回應結果

## 測試執行順序建議 (Recommended Test Execution Order)

按照以下順序執行測試，確保依賴關係正確：

```
1. health-check.http        (隨時可執行)
2. us1-acceptance.http      (公開端點，隨時可執行)
3. us2-acceptance.http      (需要順序執行以獲取 token)
4. us3-acceptance.http      (需要先註冊/登入)
5. us4-acceptance.http      (需要已存在的貼文)
6. test-mine-filter.http    (需要多個使用者和貼文)
```

## 測試前準備 (Prerequisites)

### 1. 啟動後端服務
```bash
cd backend
./mvnw spring-boot:run
```

或使用 Docker:
```bash
docker-compose up
```

### 2. 確認服務運行
```bash
curl http://localhost/api/health
```

應該返回：
```json
{
  "status": "UP",
  "timestamp": "2026-01-30T12:34:56Z"
}
```

### 3. 檢查資料庫連線
確保 PostgreSQL 正在運行並且種子資料已載入。

## 測試文件說明 (Test Files Description)

| 文件名 | 用途 | 需要認證 |
|-------|------|---------|
| `health-check.http` | 健康檢查 | ❌ |
| `us1-acceptance.http` | 瀏覽貼文和留言 | ❌ |
| `us2-acceptance.http` | 註冊、登入、個人資料 | 部分 |
| `us3-acceptance.http` | 建立和管理貼文 | ✅ |
| `us4-acceptance.http` | 建立留言 | ✅ |
| `test-mine-filter.http` | 篩選個人貼文 | ✅ |

## 重要提示 (Important Notes)

### Token 管理
- Access Token 有效期：1 小時
- 如果測試失敗並顯示 401，需要重新登入獲取新的 token
- 在 `.http` 文件中使用 `{{accessToken}}` 變數存儲 token

### Rate Limiting
- `/api/auth/register` 和 `/api/auth/login` 有速率限制
- 每 60 秒最多 5 次請求
- 如果收到 429 錯誤，請等待 60 秒後重試

### Base URL
- 本地開發：`http://localhost/api`
- 所有端點都有 `/api` 前綴（在 application.yml 中配置）

## 驗證測試覆蓋 (Verify Test Coverage)

查看完整測試對應：
```bash
cat tests/e2e/TEST-COVERAGE-MAPPING-ZH.md
```

查看詳細文檔：
```bash
cat tests/e2e/README.md
```

## 常見問題排除 (Troubleshooting)

### 問題：Connection Refused
**解決方案：**
- 確認後端服務正在運行
- 檢查端口 8080 是否可訪問
- 確認 nginx 或反向代理配置正確

### 問題：401 Unauthorized
**解決方案：**
- Token 可能已過期（1小時有效期）
- 重新執行登入測試獲取新 token
- 確認 `Authorization: Bearer <token>` header 格式正確

### 問題：404 Not Found
**解決方案：**
- 檢查資源是否存在於資料庫
- 驗證 UUID 格式是否正確
- 資源可能已被軟刪除

### 問題：429 Too Many Requests
**解決方案：**
- 等待 60 秒後重試
- 速率限制：認證端點每 60 秒 5 次請求

### 問題：Database Connection Failed
**解決方案：**
- 確認 PostgreSQL 正在運行
- 檢查 `application.yml` 中的連線設定
- 驗證種子資料是否已載入

## 測試結果預期 (Expected Test Results)

### 成功標準
- ✅ 所有健康檢查返回 200
- ✅ 註冊成功返回 201 with tokens
- ✅ 登入成功返回 200 with tokens
- ✅ 建立貼文/留言返回 201
- ✅ 更新貼文返回 200
- ✅ 刪除貼文返回 204
- ✅ 無效請求正確返回 400
- ✅ 未授權請求正確返回 401
- ✅ 禁止訪問正確返回 403
- ✅ 資源不存在正確返回 404
- ✅ 重複資源正確返回 409
- ✅ 超過速率限制正確返回 429

## 獲取幫助 (Get Help)

如需更多資訊，請查看：
1. `README.md` - 完整文檔
2. `TEST-COVERAGE-MAPPING-ZH.md` - 測試對應表
3. OpenAPI 文檔：`http://localhost/api/swagger-ui/index.html`

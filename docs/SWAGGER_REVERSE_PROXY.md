# Swagger UI 反向代理配置說明

本文件說明 Swagger UI 在 Nginx 反向代理後的常見問題及解決方案。

## 常見問題

### 1. 資源路徑錯誤 (404 錯誤)

**症狀：** Swagger UI 頁面載入，但 CSS/JS 或 API 呼叫回傳 404

**原因：** 反向代理改變了請求路徑，但 Swagger UI 不知道它在代理後面

**解決方案：**
- ✅ 在 nginx 設定 `X-Forwarded-Prefix` header
- ✅ 在 Spring Boot 啟用 `server.forward-headers-strategy: framework`

### 2. 混合內容錯誤 (Mixed Content)

**症狀：** HTTPS 下載入 HTTP 資源被瀏覽器阻擋

**原因：** Nginx 終止 HTTPS 但轉發 HTTP 到後端，Swagger UI 生成錯誤的 URL

**解決方案：**
- ✅ 設定 `X-Forwarded-Proto` header
- ✅ Spring Boot 會使用這個 header 生成正確的 scheme

### 3. 無限重定向

**症狀：** 瀏覽器顯示重定向次數過多

**原因：** Nginx 和後端對於尾部斜線（trailing slash）處理不一致

**解決方案：**
- ✅ 確保 Nginx location 和 Spring Boot path 的斜線一致
- ✅ 我們的配置使用 `/api/` 和 `context-path: /api` 保持一致

### 4. CORS 錯誤

**症狀：** 跨域請求被阻擋

**原因：** 由於配置錯誤，請求被發送到非預期的 host/protocol

**解決方案：**
- ✅ 正確設定所有 `X-Forwarded-*` headers
- ✅ 如需要，在 Spring Boot 配置 CORS

## 我們的配置

### Nginx 配置 (`nginx/default.conf`)

```nginx
location /api/ {
    # Docker DNS resolver
    resolver 127.0.0.11 valid=30s;
    set $backend "app:8080";
    proxy_pass http://$backend/api/;
    
    # 關鍵 headers
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-Prefix /api;  # 重要！告訴 Spring Boot 前綴路徑
}
```

### Spring Boot 配置 (`application.yml`)

```yaml
server:
  port: 8080
  servlet:
    context-path: /api
  forward-headers-strategy: framework  # 重要！啟用 forward headers 支援

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui/index.html
    enabled: true
  use-root-path: false
```

## 測試步驟

### 1. 啟動所有服務

```bash
docker compose up -d --build
```

### 2. 等待後端啟動（首次需下載依賴）

```bash
docker compose logs -f app
# 看到 "Started SocialPlatformApplication" 表示啟動成功
```

### 3. 測試 Health API

```bash
# 直接測試後端（繞過 nginx）
curl http://localhost:8080/api/health

# 透過 nginx 反向代理
curl http://localhost/api/health
```

### 4. 測試 Swagger UI

```bash
# 檢查 Swagger UI 頁面
curl -I http://localhost/api/swagger-ui/index.html

# 檢查 OpenAPI JSON
curl http://localhost/api/api-docs
```

### 5. 在瀏覽器中訪問

開啟瀏覽器訪問：
- http://localhost/api/swagger-ui/index.html

應該看到完整的 Swagger UI 介面，所有資源（CSS/JS）都正確載入。

## 故障排除

### Swagger UI 顯示空白頁面

**檢查步驟：**

1. 開啟瀏覽器開發者工具 (F12) → Network tab
2. 重新載入頁面
3. 檢查是否有 404 或 CORS 錯誤

**常見原因：**
- 後端尚未完全啟動
- 路徑配置錯誤
- Forward headers 未正確設定

### API 文件顯示錯誤的伺服器 URL

**症狀：** Swagger UI 中的 "Try it out" 功能指向錯誤的 URL

**檢查：**
```bash
# 查看 OpenAPI JSON 中的 servers 欄位
curl http://localhost/api/api-docs | jq '.servers'
```

**預期結果：**
```json
[
  {
    "url": "http://localhost/api",
    "description": "Generated server url"
  }
]
```

如果 URL 不正確，檢查：
- `X-Forwarded-Proto` header 是否正確
- `X-Forwarded-Prefix` header 是否設定
- `server.forward-headers-strategy` 是否啟用

### 無法連接到後端

**檢查：**

```bash
# 1. 檢查服務狀態
docker compose ps

# 2. 檢查後端日誌
docker compose logs app | tail -50

# 3. 檢查 nginx 日誌
docker compose logs nginx | tail -20

# 4. 測試容器間網路
docker compose exec nginx ping app
```

## 參考資源

- [Spring Boot Forward Headers](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.webserver.use-behind-a-proxy-server)
- [SpringDoc OpenAPI Configuration](https://springdoc.org/#properties)
- [Nginx Proxy Headers](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)

## 關鍵要點總結

✅ **必須設定的 Headers：**
- `X-Forwarded-For`：原始客戶端 IP
- `X-Forwarded-Proto`：原始協議 (http/https)
- `X-Forwarded-Prefix`：路徑前綴 (本專案為 `/api`)
- `Host`：原始 Host header

✅ **Spring Boot 必須配置：**
- `server.forward-headers-strategy: framework`

✅ **路徑一致性：**
- Nginx: `location /api/` 
- Spring Boot: `context-path: /api`
- Swagger UI: 可透過 `/api/swagger-ui/index.html` 存取

這樣的配置確保 Swagger UI 能正確地在反向代理後工作。

# Runtime Error Fix: Axios Instance Access

## 問題 (Issue)

Runtime error 發生在 `client.ts:61`:
```
Uncaught TypeError: Cannot read properties of undefined (reading 'request')
    at configureApiClient (client.ts:61:26)
```

具體錯誤位置：
```typescript
return heyClient.request(originalRequest);  // ❌ heyClient.request is undefined
```

## 原因 (Root Cause)

Hey API client (`@hey-api/client-axios`) 包裝了 axios，但**不直接暴露** axios 的所有方法。

具體來說：
- `heyClient` 是由 `createClient()` 創建的包裝器
- `heyClient.interceptors` 存在，但指向底層 axios 實例
- `heyClient.request()` **不存在**
- 需要通過 `heyClient.instance` 存取底層的 axios 實例

## 解決方案 (Solution)

### Before (錯誤) ❌
```typescript
export const configureApiClient = () => {
  heyClient.setConfig({ baseURL: '/api' });

  // ❌ 錯誤：直接在 heyClient 上使用 interceptors
  heyClient.interceptors.request.use((config) => {
    // ...
  });

  heyClient.interceptors.response.use(
    (response) => response,
    async (error) => {
      // ...
      // ❌ 錯誤：heyClient.request 不存在
      return heyClient.request(originalRequest);
    }
  );
};
```

### After (正確) ✅
```typescript
export const configureApiClient = () => {
  heyClient.setConfig({ baseURL: '/api' });

  // ✅ 正確：通過 .instance 存取底層 axios
  const axiosInstance = heyClient.instance;

  axiosInstance.interceptors.request.use((config) => {
    const token = getAccessToken();
    if (token) {
      config.headers = config.headers || {};
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  });

  axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true;

        // Single-flight refresh
        if (!isRefreshing) {
          isRefreshing = true;
          refreshPromise = refreshAccessToken();
        }

        const newToken = await refreshPromise;
        isRefreshing = false;
        refreshPromise = null;

        if (newToken) {
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
          // ✅ 正確：使用 axiosInstance.request
          return axiosInstance.request(originalRequest);
        }

        return Promise.reject(error);
      }

      return Promise.reject(error);
    }
  );
};
```

## Hey API Client 結構

```
heyClient (Hey API wrapper)
├── .setConfig()       ✅ 設定 baseURL 等
├── .get()             ✅ HTTP GET
├── .post()            ✅ HTTP POST
├── .patch()           ✅ HTTP PATCH
├── .delete()          ✅ HTTP DELETE
├── .instance          ✅ 底層 axios 實例
│   ├── .interceptors  ✅ Axios interceptors
│   ├── .request()     ✅ 通用請求方法
│   └── ...
└── .request()         ❌ 不存在！
```

## 關鍵改動 (Key Changes)

1. **存取 axios 實例**
   ```typescript
   const axiosInstance = heyClient.instance;
   ```

2. **在 axios 實例上設置 interceptors**
   ```typescript
   axiosInstance.interceptors.request.use(...)
   axiosInstance.interceptors.response.use(...)
   ```

3. **使用 axios 實例重試請求**
   ```typescript
   return axiosInstance.request(originalRequest);
   ```

## 驗證 (Verification)

修正後：
- ✅ Dev server 正常啟動
- ✅ 頁面可以正常顯示
- ✅ Axios interceptors 正常運作
- ✅ Token 自動附加到請求 header
- ✅ 401 錯誤觸發 token refresh 並重試

## 相關檔案 (Related Files)

- `frontend/src/api/client.ts` - 主要修正檔案

## Commit

```
9ffb356 - fix: Access axios instance from Hey API client for interceptors
```

## 參考 (References)

- Hey API client 包裝 axios，需要通過 `.instance` 存取底層實例
- Axios interceptors 需要在 axios 實例上設置，不是在 wrapper 上
- 401 token refresh pattern 需要能夠重新執行原始請求

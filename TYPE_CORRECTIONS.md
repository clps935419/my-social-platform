# API Type Corrections

## 問題 (Issue)

使用者指出 login payload 使用了錯誤的欄位名稱：
```typescript
// 錯誤的 payload
{ phoneE164: "+886912345678", password: "password123" }

// 應該是
{ phoneNumber: "+886912345678", password: "password123" }
```

同時也沒有充分利用 Hey API 產生的型別定義。

## 原因 (Root Cause)

1. **欄位名稱錯誤**: 使用了 `phoneE164` 而不是 OpenAPI spec 定義的 `phoneNumber`
2. **未使用產生的型別**: 沒有使用 Hey API 自動產生的 `LoginRequest`、`RegisterRequest`、`AuthResponse` 等型別
3. **型別不一致**: 混用了不存在的 `LoginResponse` 與實際的 `AuthResponse`

## 解決方案 (Solution)

### 1. AuthDialog.vue - 修正 Payload 與型別

#### Before ❌
```typescript
import type { LoginResponse } from '../api/generated/types.gen';

const phoneE164 = `${form.region}${form.phone}`;

const response = await login({
  body: {
    phoneE164,            // ❌ 錯誤的欄位名稱
    password: form.password,
  },
});

const data = response.data as LoginResponse;  // ❌ 型別不存在
```

#### After ✅
```typescript
import type { LoginRequest, RegisterRequest, AuthResponse } from '../api/generated/types.gen';

const phoneNumber = `${form.region}${form.phone}`;

const loginPayload: LoginRequest = {
  phoneNumber,           // ✅ 正確的欄位名稱
  password: form.password,
};

const response = await login({
  body: loginPayload,    // ✅ 使用型別化的 payload
});

const data = response.data as AuthResponse;  // ✅ 正確的型別
```

### 2. session.ts - 使用正確的使用者型別

#### Before ❌
```typescript
import type { UserProfile } from '../api/generated/types.gen';  // ❌ 型別不存在

export interface SessionState {
  user: UserProfile | null;  // ❌
}
```

#### After ✅
```typescript
import type { UserInfo } from '../api/generated/types.gen';  // ✅ 正確的型別

export interface SessionState {
  user: UserInfo | null;  // ✅ 來自 AuthResponse.user
}
```

### 3. queries/me.ts - 使用正確的回應型別

#### Before ❌
```typescript
import type { UserProfile } from '../api/generated/types.gen';  // ❌ 型別不存在

return useQuery<UserProfile | null>({
  queryFn: async () => {
    const response = await getProfile();
    return response.data as UserProfile;  // ❌
  },
});
```

#### After ✅
```typescript
import type { UserProfileResponse } from '../api/generated/types.gen';  // ✅

return useQuery<UserProfileResponse | null>({
  queryFn: async () => {
    const response = await getProfile();
    return response.data as UserProfileResponse;  // ✅
  },
});
```

## API 型別對應表

### 請求型別 (Request Types)
| 用途 | 型別 | 欄位 |
|------|------|------|
| 登入 | `LoginRequest` | `phoneNumber`, `password` |
| 註冊 | `RegisterRequest` | `phoneNumber`, `userName`, `password`, `email?`, `coverImage?`, `biography?` |
| Token 刷新 | `RefreshRequest` | `refreshToken` |

### 回應型別 (Response Types)
| API Endpoint | 型別 | 包含 |
|--------------|------|------|
| `/auth/login` | `AuthResponse` | `accessToken`, `refreshToken`, `user: UserInfo` |
| `/auth/register` | `AuthResponse` | 同上 |
| `/me` | `UserProfileResponse` | `userId`, `phoneNumber`, `userName`, `email?`, `coverImage?`, `biography?` |

### 使用者資料型別
| 型別 | 來源 | 用途 |
|------|------|------|
| `UserInfo` | `AuthResponse.user` | Session 儲存 |
| `UserProfileResponse` | `/me` endpoint | 個人資料查詢 |

**Note**: `UserInfo` 和 `UserProfileResponse` 結構相同，但語義不同。

## 修正的檔案 (Fixed Files)

1. **frontend/src/components/AuthDialog.vue**
   - 修正 payload 欄位：`phoneE164` → `phoneNumber`
   - 使用 `LoginRequest` 和 `RegisterRequest` 型別
   - 使用 `AuthResponse` 而非不存在的 `LoginResponse`
   - 明確的型別註解提供更好的 IDE 支援

2. **frontend/src/auth/session.ts**
   - `UserProfile` → `UserInfo`
   - 與 `AuthResponse.user` 型別一致

3. **frontend/src/queries/me.ts**
   - `UserProfile` → `UserProfileResponse`
   - 與 `/me` endpoint 回應型別一致

## 好處 (Benefits)

### 1. 型別安全
```typescript
// ✅ TypeScript 會在編譯時檢查欄位名稱
const payload: LoginRequest = {
  phoneNumber: "+886912345678",
  password: "password123"
};

// ❌ TypeScript 會報錯
const payload: LoginRequest = {
  phoneE164: "+886912345678",  // Error: 'phoneE164' 不存在
  password: "password123"
};
```

### 2. IDE 自動完成
使用 Hey API 產生的型別，IDE 可以提供精確的自動完成建議。

### 3. 重構安全
當 OpenAPI spec 改變時，Hey API 重新產生型別後，TypeScript 會立即標記所有不相容的程式碼。

### 4. 文檔即程式碼
型別定義直接來自 OpenAPI spec，無需額外查詢 API 文檔。

## 驗證 (Verification)

修正後：
- ✅ Login payload 使用正確的 `phoneNumber` 欄位
- ✅ 所有型別都來自 Hey API 產生的檔案
- ✅ TypeScript 編譯無錯誤
- ✅ IDE 提供正確的型別提示
- ✅ API 請求格式符合 OpenAPI spec

## Commit

```
b719c00 - fix: Use correct API types - phoneNumber instead of phoneE164, proper type imports
```

## 總結

這次修正展示了使用 Hey API 自動產生型別的重要性：
1. 確保前端與後端契約一致
2. 編譯時捕獲 API 不匹配的問題
3. 提供優秀的開發者體驗 (IDE 支援)
4. 減少執行時錯誤

# E2E API Testing Suite

This directory contains comprehensive end-to-end (e2e) tests for all API endpoints in the Social Platform backend.

## Test Files Overview

### health-check.http
Tests for the health check endpoint.
- **GET /api/health**: Verify service is up and running (200 OK)

### us1-acceptance.http
Tests for **User Story 1: Browse Posts and Comments** (Public Access - No Authentication Required)

#### GET /api/posts
- ✅ List all posts with default pagination (200 OK)
- ✅ List posts with custom limit (200 OK)
- ✅ List posts with offset pagination (200 OK)
- ✅ List posts sorted by oldest first (200 OK)
- ✅ Invalid limit too high (400 BAD_REQUEST)
- ✅ Invalid limit negative (400 BAD_REQUEST)
- ✅ Invalid offset negative (400 BAD_REQUEST)
- ✅ Invalid sort parameter (400 BAD_REQUEST)
- ✅ Verify soft-deleted posts are excluded (200 OK)

#### GET /api/posts/{postId}/comments
- ✅ List comments for a post (200 OK)
- ✅ List comments with pagination (200 OK)
- ✅ List comments sorted newest first (200 OK)
- ✅ Comments for non-existent post (404 NOT_FOUND)
- ✅ Comments with invalid post ID format (400 BAD_REQUEST)
- ✅ Comments on soft-deleted post (404 NOT_FOUND)

### us2-acceptance.http
Tests for **User Story 2: Registration, Login, and Authentication**

#### POST /api/auth/register
- ✅ Register new user successfully (201 CREATED)
- ✅ Register with duplicate phone number (409 CONFLICT)
- ✅ Register with invalid phone number format (400 BAD_REQUEST)
- ✅ Register with phone number containing separators (201 CREATED - normalization test)
- ✅ Rate limit test - register (429 TOO_MANY_REQUESTS after 5 requests)

#### POST /api/auth/login
- ✅ Login with correct credentials (200 OK)
- ✅ Login with wrong password (401 UNAUTHORIZED)
- ✅ Login with non-existent phone number (401 UNAUTHORIZED)
- ✅ Login with normalized phone number (200 OK)
- ✅ Rate limit test - login (429 TOO_MANY_REQUESTS after 5 requests)

#### GET /api/me
- ✅ Get user profile with valid token (200 OK)
- ✅ Get user profile without token (401 UNAUTHORIZED)
- ✅ Get user profile with invalid token (401 UNAUTHORIZED)

#### POST /api/auth/refresh
- ✅ Refresh token successfully (200 OK)
- ✅ Try to use old refresh token after rotation (401 UNAUTHORIZED)
- ✅ Refresh with invalid token (401 UNAUTHORIZED)

### us3-acceptance.http
Tests for **User Story 3: Create and Manage Posts** (Authentication Required)

#### POST /api/posts
- ✅ Create post successfully (201 CREATED)
- ✅ Create post without authentication (401 UNAUTHORIZED)
- ✅ Create post with empty content (400 BAD_REQUEST)
- ✅ Create post with whitespace-only content (400 BAD_REQUEST)
- ✅ Create post with invalid image URL (400 BAD_REQUEST)
- ✅ Create post with image URL too long (400 BAD_REQUEST)
- ✅ Create post without image (201 CREATED - optional field)

#### PATCH /api/posts/{postId}
- ✅ Update own post successfully (200 OK)
- ✅ Update post with partial data (200 OK)
- ✅ Update another user's post (403 FORBIDDEN)
- ✅ Update deleted post (404 NOT_FOUND)
- ✅ Update with invalid postId format (400 BAD_REQUEST)

#### DELETE /api/posts/{postId}
- ✅ Delete own post successfully (204 NO_CONTENT)
- ✅ Delete another user's post (403 FORBIDDEN)
- ✅ Delete already deleted post (404 NOT_FOUND)
- ✅ Delete non-existent post (404 NOT_FOUND)
- ✅ Delete with invalid postId format (400 BAD_REQUEST)

### us4-acceptance.http
Tests for **User Story 4: Create Comments** (Authentication Required)

#### POST /api/posts/{postId}/comments
- ✅ Create comment successfully (201 CREATED)
- ✅ Create comment without authentication (401 UNAUTHORIZED)
- ✅ Create comment with empty content (400 BAD_REQUEST)
- ✅ Create comment with whitespace-only content (400 BAD_REQUEST)
- ✅ Create comment on non-existent post (404 NOT_FOUND)
- ✅ Create comment with invalid postId format (400 BAD_REQUEST)
- ✅ Create comment on soft-deleted post (404 NOT_FOUND)

#### GET /api/posts/{postId}/comments
(Covered in us1-acceptance.http as public endpoint)

### test-mine-filter.http
Tests for **Advanced Feature: Filter User's Own Posts**

#### GET /api/posts?mine=true
- ✅ Get only authenticated user's posts (200 OK)
- ✅ Try to use mine=true without authentication (401 UNAUTHORIZED)
- ✅ Verify different users see only their own posts (200 OK)

## How to Run Tests

### Prerequisites
1. Ensure the backend server is running on `http://localhost` (or update the base URL in test files)
2. Ensure the database is set up with seed data (if required)
3. Install a REST client that supports `.http` files:
   - [VS Code REST Client Extension](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
   - [IntelliJ IDEA HTTP Client](https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html)

### Running Tests

#### Using VS Code REST Client
1. Open any `.http` file
2. Click "Send Request" link above each test
3. View responses in the right panel

#### Using IntelliJ IDEA
1. Open any `.http` file
2. Click the green play button next to each request
3. View responses in the bottom panel

### Test Execution Order

For dependent tests, follow this order:
1. **health-check.http** - Can run anytime
2. **us1-acceptance.http** - Can run anytime (public endpoints)
3. **us2-acceptance.http** - Run sequentially for token-dependent tests
4. **us3-acceptance.http** - Requires user registration/login first
5. **us4-acceptance.http** - Requires posts to exist
6. **test-mine-filter.http** - Requires multiple users and posts

## Test Coverage Summary

| Feature | Endpoint | Total Tests | Status |
|---------|----------|-------------|--------|
| Health Check | GET /api/health | 2 | ✅ Complete |
| Browse Posts | GET /api/posts | 9 | ✅ Complete |
| Browse Comments | GET /api/posts/{postId}/comments | 6 | ✅ Complete |
| Register | POST /api/auth/register | 5 | ✅ Complete |
| Login | POST /api/auth/login | 5 | ✅ Complete |
| Get Profile | GET /api/me | 3 | ✅ Complete |
| Refresh Token | POST /api/auth/refresh | 3 | ✅ Complete |
| Create Post | POST /api/posts | 7 | ✅ Complete |
| Update Post | PATCH /api/posts/{postId} | 5 | ✅ Complete |
| Delete Post | DELETE /api/posts/{postId} | 5 | ✅ Complete |
| Create Comment | POST /api/posts/{postId}/comments | 7 | ✅ Complete |
| Filter My Posts | GET /api/posts?mine=true | 3 | ✅ Complete |
| **TOTAL** | **All Endpoints** | **60** | **✅ Complete** |

## Status Codes Tested

- ✅ 200 OK - Successful GET/PATCH/POST with response body
- ✅ 201 CREATED - Successful resource creation
- ✅ 204 NO_CONTENT - Successful DELETE
- ✅ 400 BAD_REQUEST - Invalid input/parameters
- ✅ 401 UNAUTHORIZED - Missing or invalid authentication
- ✅ 403 FORBIDDEN - Insufficient permissions
- ✅ 404 NOT_FOUND - Resource not found
- ✅ 409 CONFLICT - Duplicate resource
- ✅ 429 TOO_MANY_REQUESTS - Rate limit exceeded

## Notes

### API Base URL
All endpoints are prefixed with `/api` as configured in `application.yml`:
```yaml
server:
  servlet:
    context-path: /api
```

### Authentication
- Access tokens are JWT tokens with 1-hour expiration
- Refresh tokens have 30-day expiration
- Refresh token rotation is implemented (old token invalidated after refresh)
- Include in requests as: `Authorization: Bearer <token>`

### Rate Limiting
- `/api/auth/register` and `/api/auth/login` are rate-limited
- Maximum 5 requests per 60 seconds per endpoint

### Phone Number Format
- E.164 format required: `+[country code][number]`
- Example: `+886912345678`
- Phone numbers with separators are normalized automatically

### Pagination
- Default: `limit=20, offset=0`
- Maximum limit: `100`
- Parameters: `?limit=<N>&offset=<M>`

### Sorting
- Posts: `sort=newest` (default) or `sort=oldest`
- Comments: `sort=oldest` (default) or `sort=newest`

### Soft Delete
- Deleted posts/comments are not permanently removed
- They are marked with `deleted_at` timestamp
- Soft-deleted items are excluded from public listings
- Attempting to access soft-deleted items returns 404

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Verify backend server is running: `docker-compose up` or `./mvnw spring-boot:run`
   - Check if port 8080 is accessible

2. **401 Unauthorized**
   - Token may have expired (1-hour expiration)
   - Re-run login test to get fresh token
   - Ensure `Authorization: Bearer <token>` header is present

3. **404 Not Found**
   - Check if resource exists in database
   - Verify the ID format is correct (UUID)
   - Resource may have been soft-deleted

4. **429 Too Many Requests**
   - Wait 60 seconds before retrying
   - Rate limit: 5 requests per 60 seconds for auth endpoints

5. **Database Connection Issues**
   - Ensure PostgreSQL is running
   - Check connection settings in `application.yml`
   - Verify seed data is loaded

## Maintenance

When adding new API endpoints or features:
1. Create appropriate test cases in relevant `.http` file
2. Update this README with new test coverage
3. Follow existing test naming conventions
4. Include expected status codes and response format
5. Update the test coverage summary table

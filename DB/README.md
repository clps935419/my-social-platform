# Database Scripts

This directory contains all database initialization scripts for the social platform.

## Script Loading Order

Docker Compose's `docker-entrypoint-initdb.d` loads scripts in alphanumeric order:

1. `001_schema.sql` - Database schema and extensions
2. `010_tables.sql` - Core tables (users, posts, comments, refresh_tokens)
3. `020_indexes.sql` - Indexes and constraints
4. `030_seed.sql` - Initial seed data
5. `100_sp_user.sql` - User stored procedures
6. `110_sp_refresh_token.sql` - Refresh token stored procedures
7. `200_sp_post.sql` - Post stored procedures
8. `210_sp_comment.sql` - Comment stored procedures

## Architecture Note

All data access must go through stored procedures (SP-first approach):
- No ORM/JPA direct CRUD
- No SQL string concatenation
- All queries parameterized via SimpleJdbcCall/JdbcTemplate

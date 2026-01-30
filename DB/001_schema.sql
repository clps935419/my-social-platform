-- Database Schema and Extensions
-- UTC timezone enforcement and timestamptz usage

-- Set timezone to UTC for this session
SET timezone = 'UTC';

-- Enable UUID extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Ensure all timestamp columns will be timestamptz by default
-- (PostgreSQL default is timestamp without time zone, we want WITH time zone)

-- Migration: Remove access_period_months and valid_until from course_combos
-- Date: 2025-09-03
-- Description: Remove deprecated columns after business logic change

-- Drop the columns if they exist
ALTER TABLE course_combos DROP COLUMN IF EXISTS access_period_months;
ALTER TABLE course_combos DROP COLUMN IF EXISTS valid_until;

-- Verify the table structure
-- \d+ course_combos



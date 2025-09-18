-- liquibase formatted sql

-- Enable UUID generation (if not already enabled)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ===========================
-- Insert Categories
-- ===========================
-- changeset yulia:insert_categories
INSERT INTO categories (id, name) VALUES
                                      (gen_random_uuid(), 'Sedan'),
                                      (gen_random_uuid(), 'SUV'),
                                      (gen_random_uuid(), 'Hatchback'),
                                      (gen_random_uuid(), 'Convertible'),
                                      (gen_random_uuid(), 'Van');



-- liquibase formatted sql

-- Enable UUID generation (if not already enabled)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ===========================
-- Insert Cars
-- ===========================

-- changeset yulia:insert_categories
INSERT INTO cars (id, name, model, daily_rate, available, category_id) VALUES
                                                                           (gen_random_uuid(), 'Toyota', 'Camry', 55.00, TRUE, (SELECT id FROM categories WHERE name = 'Sedan')),
                                                                           (gen_random_uuid(), 'Honda', 'Civic', 50.00, TRUE, (SELECT id FROM categories WHERE name = 'Sedan')),
                                                                           (gen_random_uuid(), 'BMW', 'X5', 120.00, TRUE, (SELECT id FROM categories WHERE name = 'SUV')),
                                                                           (gen_random_uuid(), 'Audi', 'Q7', 140.00, TRUE, (SELECT id FROM categories WHERE name = 'SUV')),
                                                                           (gen_random_uuid(), 'Volkswagen', 'Golf', 45.00, TRUE, (SELECT id FROM categories WHERE name = 'Hatchback')),
                                                                           (gen_random_uuid(), 'Mazda', 'MX-5', 90.00, TRUE, (SELECT id FROM categories WHERE name = 'Convertible')),
                                                                           (gen_random_uuid(), 'Mercedes', 'Vito', 100.00, TRUE, (SELECT id FROM categories WHERE name = 'Van'));
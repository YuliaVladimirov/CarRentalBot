-- liquibase formatted sql

-- Enable UUID generation (if not already enabled)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ===========================
-- INSERT CARS
-- ===========================

-- changeset yulia:insert_categories
INSERT INTO cars (id, brand, model, category, description, image_url, daily_rate, available) VALUES
                                                                                                (gen_random_uuid(), 'Toyota', 'Camry', 'SEDAN',
                                                                                                 'Reliable midsize sedan with great fuel efficiency.',
                                                                                                 'https://example.com/images/camry.jpg',
                                                                                                 55.00, TRUE),
                                                                                                (gen_random_uuid(), 'Hyundai', 'Elantra', 'SEDAN',
                                                                                                 'Compact sedan with modern design and safety features.',
                                                                                                 'https://example.com/images/elantra.jpg',
                                                                                                 50.00, FALSE),
                                                                                                (gen_random_uuid(), 'Tesla', 'Model 3', 'SEDAN',
                                                                                                 'Electric sedan with autopilot and excellent range.',
                                                                                                 'https://example.com/images/model3.jpg',
                                                                                                 95.00, TRUE),

                                                                                                (gen_random_uuid(), 'Honda', 'Civic', 'HATCHBACK',
                                                                                                 'Compact hatchback, sporty look, efficient on gas.',
                                                                                                 'https://example.com/images/civic.jpg',
                                                                                                 45.00, TRUE),
                                                                                                (gen_random_uuid(), 'Volkswagen', 'Golf', 'HATCHBACK',
                                                                                                 'Classic European hatchback, practical and reliable.',
                                                                                                 'https://example.com/images/golf.jpg',
                                                                                                 48.00, TRUE),
                                                                                                (gen_random_uuid(), 'Ford', 'Focus', 'HATCHBACK',
                                                                                                 'Economical hatchback, good for city trips.',
                                                                                                 'https://example.com/images/focus.jpg',
                                                                                                 42.00, FALSE),

                                                                                                (gen_random_uuid(), 'BMW', 'X5', 'SUV',
                                                                                                 'Luxury SUV with premium interior and advanced features.',
                                                                                                 'https://example.com/images/bmw-x5.jpg',
                                                                                                 120.00, TRUE),
                                                                                                (gen_random_uuid(), 'Toyota', 'RAV4', 'SUV',
                                                                                                 'Reliable mid-size SUV with hybrid option.',
                                                                                                 'https://example.com/images/rav4.jpg',
                                                                                                 80.00, TRUE),
                                                                                                (gen_random_uuid(), 'Jeep', 'Wrangler', 'SUV',
                                                                                                 'Off-road SUV, rugged and adventurous.',
                                                                                                 'https://example.com/images/wrangler.jpg',
                                                                                                 100.00, FALSE),

                                                                                                (gen_random_uuid(), 'Ford', 'Mustang Convertible', 'CONVERTIBLE',
                                                                                                 'Iconic American muscle car with open-top driving experience.',
                                                                                                 'https://example.com/images/mustang.jpg',
                                                                                                 150.00, TRUE),
                                                                                                (gen_random_uuid(), 'Mazda', 'MX-5 Miata', 'CONVERTIBLE',
                                                                                                 'Lightweight roadster, fun to drive.',
                                                                                                 'https://example.com/images/mx5.jpg',
                                                                                                 110.00, TRUE),
                                                                                                (gen_random_uuid(), 'Audi', 'A5 Cabriolet', 'CONVERTIBLE',
                                                                                                 'Luxury convertible with refined style.',
                                                                                                 'https://example.com/images/a5.jpg',
                                                                                                 135.00, FALSE),

                                                                                                (gen_random_uuid(), 'Mercedes-Benz', 'V-Class', 'VAN',
                                                                                                 'Luxury van with spacious seating for 7 passengers.',
                                                                                                 'https://example.com/images/vclass.jpg',
                                                                                                 95.00, TRUE),
                                                                                                (gen_random_uuid(), 'Volkswagen', 'Transporter', 'VAN',
                                                                                                 'Practical van for cargo and family trips.',
                                                                                                 'https://example.com/images/transporter.jpg',
                                                                                                 85.00, TRUE),
                                                                                                (gen_random_uuid(), 'Ford', 'Transit', 'VAN',
                                                                                                 'Spacious van for groups, cargo, or long trips.',
                                                                                                 'https://example.com/images/transit.jpg',
                                                                                                 90.00, FALSE);
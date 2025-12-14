-- liquibase formatted sql

-- Enable UUID generation (if not already enabled)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ===========================
-- INSERT CARS
-- ===========================

-- changeset yulia:insert_cars
INSERT INTO cars (id, brand, model, category, description, image_file_id, car_status, daily_rate)
VALUES (gen_random_uuid(), 'Toyota', 'Camry', 'SEDAN',
        'Reliable midsize sedan with great fuel efficiency.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 55.00),
       (gen_random_uuid(), 'Hyundai', 'Elantra', 'SEDAN',
        'Compact sedan with modern design and safety features.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 50.00),
       (gen_random_uuid(), 'Tesla', 'Model 3', 'SEDAN',
        'Electric sedan with autopilot and excellent range.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 95.00),

       (gen_random_uuid(), 'Honda', 'Civic', 'HATCHBACK',
        'Compact hatchback, sporty look, efficient on gas.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 45.00),
       (gen_random_uuid(), 'Volkswagen', 'Golf', 'HATCHBACK',
        'Classic European hatchback, practical and reliable.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 48.00),
       (gen_random_uuid(), 'Ford', 'Focus', 'HATCHBACK',
        'Economical hatchback, good for city trips.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 42.00),

       (gen_random_uuid(), 'BMW', 'X5', 'SUV',
        'Luxury SUV with premium interior and advanced features.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 120.00),
       (gen_random_uuid(), 'Toyota', 'RAV4', 'SUV',
        'Reliable mid-size SUV with hybrid option.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 80.00),
       (gen_random_uuid(), 'Jeep', 'Wrangler', 'SUV',
        'Off-road SUV, rugged and adventurous.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 100.00),

       (gen_random_uuid(), 'Ford', 'Mustang Convertible', 'CONVERTIBLE',
        'Iconic American muscle car with open-top driving experience.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 150.00),
       (gen_random_uuid(), 'Mazda', 'MX-5 Miata', 'CONVERTIBLE',
        'Lightweight roadster, fun to drive.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 110.00),
       (gen_random_uuid(), 'Audi', 'A5 Cabriolet', 'CONVERTIBLE',
        'Luxury convertible with refined style.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 135.00),

       (gen_random_uuid(), 'Mercedes-Benz', 'V-Class', 'VAN',
        'Luxury van with spacious seating for 7 passengers.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 95.00),
       (gen_random_uuid(), 'Volkswagen', 'Transporter', 'VAN',
        'Practical van for cargo and family trips.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 85.00),
       (gen_random_uuid(), 'Ford', 'Transit', 'VAN',
        'Spacious van for groups, cargo, or long trips.',
        'AgACAgIAAxkDAAIBKGjeYhWLCFaGmomfhnYyJeqgbknrAAL5_DEbssnxSp-EG8I5uiC2AQADAgADbQADNgQ',
        'IN_SERVICE', 90.00);
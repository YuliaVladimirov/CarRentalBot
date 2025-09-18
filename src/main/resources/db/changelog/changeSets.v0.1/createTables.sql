--liquibase formatted sql


-- ========================================
-- CUSTOMERS
-- ========================================
--changeset yulia:2025-09-06-create-customers
CREATE TABLE customers (
                           id UUID PRIMARY KEY,
                           telegram_user_id BIGINT UNIQUE NOT NULL,
                           chat_id BIGINT UNIQUE NOT NULL,
                           username VARCHAR(100),
                           first_name VARCHAR(100) NOT NULL,
                           last_name VARCHAR(100) NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset yulia:2025-09-06-index-customers-first-name
CREATE INDEX index_customers_first_name ON customers(first_name);
--changeset yulia:2025-09-06-index-customers-last-name
CREATE INDEX index_customers_last_name ON customers(last_name);


-- ========================================
-- CATEGORIES
-- ========================================
CREATE TABLE categories (
                       id UUID PRIMARY KEY,
                       name VARCHAR(100) NOT NULL
);

--changeset yulia:2025-09-06-index-categories-name
CREATE INDEX index_categories_name ON categories(name);


-- ========================================
-- CARS
-- ========================================
CREATE TABLE cars (
                      id UUID PRIMARY KEY,
                      name VARCHAR(100) NOT NULL,
                      model VARCHAR(100) NOT NULL,
                      daily_rate DECIMAL(10,2) NOT NULL,
                      available BOOLEAN DEFAULT TRUE,
                      category_id UUID NOT NULL,
                      CONSTRAINT fk_cars_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

--changeset yulia:2025-09-06-index-cars-name
CREATE INDEX index_cars_name ON cars(name);
--changeset yulia:2025-09-06-index-cars-available
CREATE INDEX index_cars_available ON cars(available);
--changeset yulia:2025-09-06-index-cars-category_id
CREATE INDEX index_cars_category_id ON cars(category_id);


-- ========================================
-- BOOKING
-- ========================================
CREATE TABLE booking (
                         id UUID PRIMARY KEY,
                         customer_id UUID NOT NULL,
                         car_id UUID NOT NULL,
                         start_time DATE NOT NULL,
                         end_time DATE NOT NULL,
                         total_cost DECIMAL(10,2) NOT NULL,
                         status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED','CANCELLED')),
                         CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
                         CONSTRAINT fk_booking_car FOREIGN KEY (car_id) REFERENCES cars(id)
);

--changeset yulia:2025-09-06-index-booking-customer_id
CREATE INDEX index_booking_customer_id ON booking(customer_id);
--changeset yulia:2025-09-06-index-booking-car_id
CREATE INDEX index_booking_car_id ON booking(car_id);
--changeset yulia:2025-09-06-index-booking-start_time
CREATE INDEX index_booking_start_time ON booking(start_time);
--changeset yulia:2025-09-06-index-booking-end_time
CREATE INDEX index_booking_end_time ON booking(end_time);
--changeset yulia:2025-09-06-index-booking-status
CREATE INDEX index_booking_status ON booking(status);


-- ========================================
-- REMINDER LOG
-- ========================================
CREATE TABLE reminder_log (
                              id SERIAL PRIMARY KEY,
                              booking_id UUID NOT NULL,
                              sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              message TEXT NOT NULL,
                              CONSTRAINT fk_reminder_log_booking FOREIGN KEY (booking_id) REFERENCES booking(id)
);

--changeset yulia:2025-09-06-index-reminder_log-booking_id
CREATE INDEX index_reminder_log_booking_id ON reminder_log(booking_id);
--changeset yulia:2025-09-06-index-reminder_log-sent_at
CREATE INDEX index_reminder_log_sent_at ON reminder_log(sent_at);

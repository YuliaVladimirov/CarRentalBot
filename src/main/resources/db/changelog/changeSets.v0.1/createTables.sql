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
                           phone_number VARCHAR(20),
                           email VARCHAR(100),
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset yulia:2025-09-06-index-customers-username
CREATE INDEX index_customers_username ON customers(username);
--changeset yulia:2025-09-06-index-customers-phone-number
CREATE INDEX index_customers_phone_number ON customers(phone_number);
--changeset yulia:2025-09-06-index-customers-email
CREATE INDEX idx_customers_email ON customers(email);


-- ========================================
-- CARS
-- ========================================
CREATE TABLE cars (
                      id UUID PRIMARY KEY,
                      brand VARCHAR(100) NOT NULL,
                      model VARCHAR(100) NOT NULL,
                      category VARCHAR(50) NOT NULL CHECK (category IN ('SEDAN', 'SUV',  'HATCHBACK', 'CONVERTIBLE', 'VAN')),
                      description VARCHAR(500),
                      image_url VARCHAR(200),
                      daily_rate DECIMAL(10,2) NOT NULL,
                      available BOOLEAN DEFAULT TRUE
);

--changeset yulia:2025-09-06-index-cars-name
CREATE INDEX index_cars_name ON cars(brand);
--changeset yulia:2025-09-06-index-cars-category
CREATE INDEX index_cars_category_id ON cars(category);
--changeset yulia:2025-09-06-index-cars-available
CREATE INDEX index_cars_available ON cars(available);



-- ========================================
-- BOOKINGS
-- ========================================
CREATE TABLE bookings (
                         id UUID PRIMARY KEY,
                         customer_id UUID NOT NULL,
                         car_id UUID NOT NULL,
                         start_date DATE NOT NULL,
                         end_date DATE NOT NULL,
                         total_cost DECIMAL(10,2) NOT NULL,
                         status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED','CANCELLED')),
                         CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
                         CONSTRAINT fk_booking_car FOREIGN KEY (car_id) REFERENCES cars(id)
);

--changeset yulia:2025-09-06-index-bookings-customer_id
CREATE INDEX index_bookings_customer_id ON bookings(customer_id);
--changeset yulia:2025-09-06-index-bookings-car_id
CREATE INDEX index_bookings_car_id ON bookings(car_id);
--changeset yulia:2025-09-06-index-bookings-start_time
CREATE INDEX index_bookings_start_time ON bookings(start_date);
--changeset yulia:2025-09-06-index-bookings-end_time
CREATE INDEX index_bookings_end_time ON bookings(end_date);
--changeset yulia:2025-09-06-index-bookings-status
CREATE INDEX index_bookings_status ON bookings(status);

-- ========================================
-- REMINDERS
-- ========================================
CREATE TABLE reminders (
                              id SERIAL PRIMARY KEY,
                              customer_id UUID NOT NULL,
                              booking_id UUID NOT NULL,
                              sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              message VARCHAR(600) NOT NULL,
                              due_at TIMESTAMP NOT NULL,
                              sent BOOLEAN NOT NULL DEFAULT FALSE,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_reminders_customers FOREIGN KEY (customer_id) REFERENCES customers (id),
                              CONSTRAINT fk_reminders_bookings FOREIGN KEY (booking_id) REFERENCES bookings (id)
);

--changeset yulia:2025-09-06-index-reminders-customer-id
CREATE INDEX index_reminders_customer_id ON reminders(customer_id);
--changeset yulia:2025-09-06-index-reminders-booking-id
CREATE INDEX index_reminders_booking_id ON reminders(booking_id);
--changeset yulia:2025-09-06-index-reminders-due-at
CREATE INDEX idx_reminder_due_at ON reminders(due_at);
--changeset yulia:2025-09-06-index-reminders-sent
CREATE INDEX idx_reminder_sent ON reminders(sent);


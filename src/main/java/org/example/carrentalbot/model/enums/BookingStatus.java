package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines the final transactional state of a customer's reservation *within the application's scope*.
 * This status determines the car's availability and the reservation's validity.
 *
 * <p>Used throughout the system for inventory management, scheduling, and customer communication.</p>
 *
 */
@Getter
public enum BookingStatus {

    /**
     * The reservation is active, validated, and secured. The booked car
     * is officially reserved for the customer and is marked as unavailable
     * for other bookings during the reserved period.
     */
    CONFIRMED ("Confirmed"),

    /**
     * The reservation has been voided by the customer or the system.
     * The car is immediately released and returned to the pool of available
     * inventory for new reservations. This status is final and stops all
     * further transaction processing.
     */
    CANCELLED ("Cancelled");

    /**
     * The user-friendly, displayable name for the booking status.
     */
    private final String value;

    /**
     * Constructs a {@code BookingStatus} with the specified display value.
     * @param value The display name to be associated with the enum constant.
     */
    BookingStatus(String value) {
        this.value = value;
    }
}

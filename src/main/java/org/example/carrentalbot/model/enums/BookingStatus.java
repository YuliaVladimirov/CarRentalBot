package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Represents the lifecycle status of a booking.
 * <p>Determines whether a booking reserves a car or has been released,
 * and affects car availability and system processing logic.</p>
 * <p>Used for scheduling, inventory management, and customer notifications.</p>
 */
@Getter
public enum BookingStatus {

    /**
     * Active booking that has been confirmed.
     * <p>The car is reserved for the customer for the specified period
     * and is not available for other bookings.</p>
     */
    CONFIRMED ("Confirmed"),

    /**
     * Booking that has been canceled.
     * <p>The reservation is invalidated and the car is released back
     * to the available inventory.</p>
     */
    CANCELLED ("Cancelled");

    /**
     * Display name of the booking status.
     */
    private final String value;

    /**
     * Creates a booking status with a display name.
     *
     * @param value human-readable status name
     */
    BookingStatus(String value) {
        this.value = value;
    }
}

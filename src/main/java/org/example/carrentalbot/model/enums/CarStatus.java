package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Represents the operational status of a car in the rental fleet.
 * <p>Determines whether a car is available for booking and rental operations.</p>
 * <p>Only {@code IN_SERVICE} cars are eligible for new bookings.</p>
 *
 * @see org.example.carrentalbot.model.Car
 */
@Getter
public enum CarStatus {

    IN_SERVICE ("In Service"),
    AWAITING_PREPARATION ("Awaiting Preparation"),
    UNDER_MAINTENANCE ("Under Maintenance"),
    UNDER_REPAIR ("Under Repair"),
    DAMAGED ("Damaged"),
    RETIRED ("Retired");

    /**
     * Display name of the car status.
     */
    private final String value;

    /**
     * Creates a car status with a display name.
     *
     * @param value the human-readable status name
     */
    CarStatus(String value) {this.value = value;}
}

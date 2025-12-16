package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines the fixed administrative and operational lifecycle status for a car
 * in the rental fleet. This status dictates whether a car is currently eligible
 * for booking and rental transactions.
 *
 * <p>Only the {@code IN_SERVICE} status permits the creation of new rental bookings.</p>
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
     * The user-friendly, displayable name for the car's status.
     */
    private final String value;

    /**
     * Constructs a {@code CarStatus} with the specified display value.
     * @param value The display name to be associated with the enum constant.
     */
    CarStatus(String value) {this.value = value;}
}

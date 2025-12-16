package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines the mode in which the car inventory is being queried and displayed to the user.
 * This enum is typically used to determine which filtering criteria should be applied to the car list.
 */
@Getter
public enum CarBrowsingMode {

    /**
     * This browsing mode selects all cars that are currently
     * {@code IN_SERVICE}, regardless of any future booking conflicts.
     * It is used when the user has not yet specified a rental period.
     * @see org.example.carrentalbot.model.enums.CarStatus
     */
    ALL_CARS("All Cars"),

    /**
     * This browsing mode applies temporal filtering, selecting
     * only cars that are {@code IN_SERVICE} AND have no confirmed bookings
     * overlapping with the user-specified start and end dates.
     * @see org.example.carrentalbot.model.enums.CarStatus
     */
    CARS_FOR_DATES("Car For Dates");

    /**
     * The user-friendly, displayable name for the browsing mode.
     */
    private final String value;

    /**
     * Constructs a {@code CarBrowsingMode} with the specified display value.
     * @param value The display name to be associated with the enum constant.
     */
    CarBrowsingMode(String value) {
        this.value = value;
    }
}

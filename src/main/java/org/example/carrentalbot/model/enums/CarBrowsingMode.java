package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines how cars are filtered and presented to the user.
 * <p>Determines whether all available cars are shown or only cars
 * available for a specific rental period.</p>
 */
@Getter
public enum CarBrowsingMode {

    /**
     * Shows all cars that are currently in service.
     * <p>No date-based filtering is applied. This mode is used when
     * the user has not selected a rental period.</p>
     */
    ALL_CARS("All Cars"),

    /**
     * Shows cars available for a selected rental period.
     * <p>Includes only cars that are in service and not reserved
     * for overlapping confirmed bookings.</p>
     */
    CARS_FOR_DATES("Car For Dates");

    /**
     * Display name of the browsing mode.
     */
    private final String value;

    /**
     * Creates a browsing mode with a display name.
     *
     * @param value human-readable mode name
     */
    CarBrowsingMode(String value) {
        this.value = value;
    }
}

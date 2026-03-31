package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Represents the available car categories in the rental system.
 * <p>Used for filtering, pricing tiers, and inventory classification.</p>
 *
 * @see org.example.carrentalbot.model.Car
 */
@Getter
public enum CarCategory {

    SEDAN("Sedan"),
    SUV("SUV"),
    HATCHBACK("Hatchback"),
    CONVERTIBLE("Convertible"),
    VAN("Van");

    /**
     * Display name of the car category.
     */
    private final String value;

    /**
     * Creates a car category with a display name.
     *
     * @param value the human-readable category name
     */
    CarCategory(String value) {
        this.value = value;
    }
}

package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines the fixed categories for cars available in the rental fleet.
 * These categories are used for car filtering, pricing tiers, and inventory management.
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
     * The user-friendly, displayable name for the car category.
     */
    private final String value;

    /**
     * Constructs a {@code CarCategory} with the specified display value.
     * @param value The display name to be associated with the enum constant.
     */
    CarCategory(String value) {
        this.value = value;
    }
}

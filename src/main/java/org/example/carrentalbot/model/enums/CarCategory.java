package org.example.carrentalbot.model.enums;

import lombok.Getter;

@Getter
public enum CarCategory {
    SEDAN("Sedan"),
    SUV("SUV"),
    HATCHBACK("Hatchback"),
    CONVERTIBLE("Convertible"),
    VAN("Van");

    private final String value;

    CarCategory(String value) {
        this.value = value;
    }
}

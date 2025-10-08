package org.example.carrentalbot.model.enums;

import lombok.Getter;

@Getter
public enum CarBrowsingMode {

    ALL_CARS("All Cars"),
    CARS_FOR_DATES("Car For Dates");

    private final String value;

    CarBrowsingMode(String value) {
        this.value = value;
    }
}

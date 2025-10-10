package org.example.carrentalbot.model.enums;

import lombok.Getter;

@Getter
public enum BookingStatus {
    CONFIRMED ("Confirmed"),
    CANCELLED ("Cancelled");

    private final String value;

    BookingStatus(String value) {
        this.value = value;
    }
}

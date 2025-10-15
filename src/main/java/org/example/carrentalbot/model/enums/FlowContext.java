package org.example.carrentalbot.model.enums;

import lombok.Getter;

@Getter
public enum FlowContext {

    BROWSING_FLOW  ("Catalog"),
    BOOKING_FLOW ("Booking"),
    EDIT_BOOKING_FLOW ("Edit Booking");

    private final String value;

    FlowContext(String value) {
        this.value = value;
    }
}

package org.example.carrentalbot.model.enums;

import lombok.Getter;

@Getter
public enum BookingNotification {

    CONFIRMATION ("Booking Confirmed ✅", "successfully confirmed."),
    UPDATE ("Booking Updated ✅", "successfully updated."),
    CANCELLATION ("Booking Canceled ✅", "successfully canceled.");

    private final String title;
    private final String message;


    BookingNotification(String title, String message) {
        this.title = title;
        this.message = message;
    }
}

package org.example.carrentalbot.model.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    CONFIRMATION(
            "Booking Confirmed ✅",
            """
                    Your booking has been successfully confirmed.
                    """),
    UPDATE(
            "Booking Updated ✅",
            """
                    Your booking has been successfully updated.
                    """),
    CANCELLATION(
            "Booking Canceled ✅",
            """
                    Your booking has been successfully canceled.
                    """);

    private final String title;
    private final String message;

    NotificationType(String title, String message) {
        this.title = title;
        this.message = message;
    }
}

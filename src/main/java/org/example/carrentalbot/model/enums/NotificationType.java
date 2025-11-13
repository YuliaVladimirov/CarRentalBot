package org.example.carrentalbot.model.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    CONFIRMATION(
            "Booking Confirmation",
            "Booking Confirmed ✅",
            """
                    Your booking has been successfully confirmed.
                    """),
    UPDATE(
            "Booking Update",
            "Booking Updated ✅",
            """
                    Your booking has been successfully updated.
                    """),
    CANCELLATION(
            "Booking Cancellation",
            "Booking Canceled ✅",
            """
                    Your booking has been successfully canceled.
                    """);

    private final String subject;
    private final String title;
    private final String message;

    NotificationType(String subject,
                     String title,
                     String message) {
        this.subject = subject;
        this.title = title;
        this.message = message;
    }
}

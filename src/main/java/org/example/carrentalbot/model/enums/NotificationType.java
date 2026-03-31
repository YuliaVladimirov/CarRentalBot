package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines notification templates used for booking-related user communication.
 * <p>Each type contains predefined text components (subject, title, and message)
 * ensuring consistent and centralized messaging across the system.</p>
 */
@Getter
public enum NotificationType {

    /**
     * Sent when a booking is successfully created and confirmed.
     */
    CONFIRMATION(
            "Booking Confirmation",
            "Booking Confirmed ✅",
            """
                    Your booking has been successfully confirmed.
                    """),
    /**
     * Sent when an existing booking is updated.
     */
    UPDATE(
            "Booking Update",
            "Booking Updated ✅",
            """
                    Your booking has been successfully updated.
                    """),
    /**
     * Sent when a booking is canceled.
     */
    CANCELLATION(
            "Booking Cancellation",
            "Booking Canceled ✅",
            """
                    Your booking has been successfully canceled.
                    """);

    /**
     * Notification subject line.
     */
    private final String subject;

    /**
     * Notification title displayed to the user.
     */
    private final String title;

    /**
     * Notification message body.
     */
    private final String message;

    /**
     * Creates a notification template with predefined text components.
     *
     * @param subject notification subject line
     * @param title notification title
     * @param message notification body content
     */
    NotificationType(String subject,
                     String title,
                     String message) {
        this.subject = subject;
        this.title = title;
        this.message = message;
    }
}

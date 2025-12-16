package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines the fixed types of notifications sent to the user regarding their
 * booking status. Each type bundles the necessary text templates (subject,
 * title, and message body) for generating consistent communications.
 * <p>This structure ensures that all notification text is centralized and type-safe.</p>
 */
@Getter
public enum NotificationType {

    /**
     * Notification sent upon successful creation and confirmation of a new booking.
     */
    CONFIRMATION(
            "Booking Confirmation",
            "Booking Confirmed ✅",
            """
                    Your booking has been successfully confirmed.
                    """),
    /**
     * Notification sent when an existing, confirmed booking is modified (e.g., phone, email).
     */
    UPDATE(
            "Booking Update",
            "Booking Updated ✅",
            """
                    Your booking has been successfully updated.
                    """),
    /**
     * Notification sent when an active reservation is canceled by the user.
     */
    CANCELLATION(
            "Booking Cancellation",
            "Booking Canceled ✅",
            """
                    Your booking has been successfully canceled.
                    """);

    /**
     * The subject line used when sending the notification via email.
     */
    private final String subject;

    /**
     * The short, bold title displayed when sending the notification via email.
     */
    private final String title;

    /**
     * The notification body message when sending the notification via email.
     */
    private final String message;

    /**
     * Constructs a {@code NotificationType} with all required text components.
     * @param subject The email subject line.
     * @param title The notification title.
     * @param message The body template of the message.
     */
    NotificationType(String subject,
                     String title,
                     String message) {
        this.subject = subject;
        this.title = title;
        this.message = message;
    }
}

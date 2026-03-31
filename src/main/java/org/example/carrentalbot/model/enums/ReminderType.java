package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines reminder templates sent to customers based on their rental period.
 * <p>Each type represents a specific moment in the rental lifecycle
 * (before start or before end) and provides predefined message content
 * for consistent communication.</p>
 */
@Getter
public enum ReminderType {

    /**
     * Sent one day before the rental start date.
     */
    START_DAY_BEFORE(
            "Rental Start Reminder ⏳",
            """
                    Dear customer, your rental starts tomorrow.
                    """),
    /**
     * Sent on the rental start date.
     */
    START_DAY_OF(
            "Rental Starting Today ⏳",
            """
                    Dear customer, your rental starts today.
                    Have a great trip!
                    """),
    /**
     * Sent one day before the rental end date.
     */
    END_DAY_BEFORE(
            "Rental Return Reminder ⏳",
            """
                    Dear customer, your rental ends tomorrow.
                    """),
    /**
     * Sent on the rental end date.
     */
    END_DAY_OF(
            "Rental Due Today ⏳",
            """
                    Dear customer, your rental ends today.
                    Please return the car on time.
                    """);

    /**
     * Reminder title displayed to the customer.
     */
    private final String title;

    /**
     * Reminder message body.
     * <p>May include dynamic booking-related information at runtime.</p>
     */
    private final String message;

    /**
     * Creates a reminder template with a title and message body.
     *
     * @param title reminder title
     * @param message reminder message template
     */
    ReminderType(String title, String message) {
        this.title = title;
        this.message = message;
    }
}

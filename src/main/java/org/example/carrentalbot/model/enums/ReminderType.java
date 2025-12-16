package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines the fixed types of scheduled reminders sent to the customer based
 * on the start and end dates of their car rental.
 * <p>Each type provides the necessary text templates (title and message body)
 * for generating consistent, timely communications.</p>
 */
@Getter
public enum ReminderType {

    /**
     * Reminder scheduled to be sent exactly <b>1 day before</b> the official
     * start date of the car rental period, triggered daily at 10:00 AM system time.
     */
    START_DAY_BEFORE(
            "Rental Start Reminder ⏳",
            """
                    Dear customer, your rental starts tomorrow.
                    """),
    /**
     * Reminder scheduled to be sent <b>on the day</b> the rental is scheduled to begin,
     * triggered daily at 10:00 AM system time.
     */
    START_DAY_OF(
            "Rental Starting Today ⏳",
            """
                    Dear customer, your rental starts today.
                    Have a great trip!
                    """),
    /**
     * Reminder scheduled to be sent exactly <b>1 day before</b> the official
     * end date of the car rental, triggered daily at 10:00 AM system time.
     */
    END_DAY_BEFORE(
            "Rental Return Reminder ⏳",
            """
                    Dear customer, your rental ends tomorrow.
                    """),
    /**
     * Reminder scheduled to be sent <b>on the day</b> the car is scheduled to be
     * returned, triggered daily at 10:00 AM system time. This message urges the
     * customer to return the vehicle promptly.
     */
    END_DAY_OF(
            "Rental Due Today ⏳",
            """
                    Dear customer, your rental ends today.
                    Please return the car on time.
                    """);

    /**
     * The short, prominent title displayed for the reminder.
     */
    private final String title;

    /**
     * The body of the reminder message. This text often precedes or
     * includes dynamic information like booking ID, rental period or total days.
     */
    private final String message;

    /**
     * Constructs a {@code ReminderType} with the specified title and message template.
     * @param title The title of the reminder notification.
     * @param message The body template of the reminder message.
     */
    ReminderType(String title, String message) {
        this.title = title;
        this.message = message;
    }
}

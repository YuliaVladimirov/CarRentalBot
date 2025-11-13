package org.example.carrentalbot.model.enums;

import lombok.Getter;

@Getter
public enum ReminderType {
    START_DAY_BEFORE(
            "Rental Start Reminder ⏳",
            """
                    Dear customer, your rental starts tomorrow.
                    """),
    START_DAY_OF(
            "Rental Starting Today ⏳",
            """
                    Dear customer, your rental starts today.
                    Have a great trip!
                    """),
    END_DAY_BEFORE(
            "Rental Return Reminder ⏳",
            """
                    Dear customer, your rental ends tomorrow.
                    """),
    END_DAY_OF(
            "Rental Due Today ⏳",
            """
                    Dear customer, your rental ends today.
                    Please return the car on time.
                    """);

    private final String title;
    private final String message;

    ReminderType(String title, String message) {
        this.title = title;
        this.message = message;
    }
}

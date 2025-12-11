package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Represents the high-level conversational context a user is currently in.
 *
 * <p>Each Telegram chat session is associated with one of these flow contexts.
 * Handlers use this information to determine whether an action is allowed at
 * a given moment (e.g., commands or callbacks restricted to a booking flow).</p>
 *
 * <p>The {@code value} field provides a human-readable label for displaying
 * or logging the active flow.</p>
 */
@Getter
public enum FlowContext {

    /** User is browsing the catalog. */
    BROWSING_FLOW  ("Catalog"),

    /** User is actively going through the booking process. */
    BOOKING_FLOW ("Booking"),

    /** User is modifying an existing booking. */
    EDIT_BOOKING_FLOW ("Edit Booking"),

    /** User is viewing or managing their existing bookings. */
    MY_BOOKINGS_FLOW ("My Bookings");

    private final String value;

    FlowContext(String value) {
        this.value = value;
    }
}

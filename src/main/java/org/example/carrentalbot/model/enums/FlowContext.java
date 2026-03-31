package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Represents the current conversational flow the user is in.
 * <p>Defines which actions and commands are allowed based on the user's
 * current step in the application workflow.</p>
 * <p>Used to enforce navigation consistency across multistep processes.</p>
 */
@Getter
public enum FlowContext {

    /**
     * User is browsing available cars and viewing the catalog.
     * <p>Only browsing-related actions are allowed.</p>
     */
    BROWSING_FLOW(
            "Catalog",
            """
                    This option is not available in browsing flow.
                    """),

    /**
     * User is in the process of creating a new booking.
     * <p>Only booking workflow actions are allowed (e.g., step navigation,
     * confirmation, or cancellation).</p>
     */
    BOOKING_FLOW(
            "Booking",
            """
                    This option is not available in booking flow.
                    """),

    /**
     * User is modifying an existing booking that is not yet confirmed.
     * <p>Only edit-related actions are allowed during this flow.</p>
     */
    EDIT_BOOKING_FLOW(
            "Edit Booking",
            """
                    This option is not available in edit flow.
                    """),

    /**
     * User is viewing their existing bookings.
     * <p>Allows viewing and limited modifications of eligible reservations.</p>
     */
    MY_BOOKINGS_FLOW(
            "My Bookings",
            """
                    This option is not available in my bookings flow.
                    """);

    /**
     * Display name of the flow context.
     */
    private final String value;

    /**
     * Message shown when a user attempts an action not allowed in this flow.
     */
    private final String errorMessage;

    /**
     * Creates a flow context with a display name and restriction message.
     *
     * @param value human-readable flow name
     * @param errorMessage message shown for invalid actions in this flow
     */
    FlowContext(String value, String errorMessage) {
        this.value = value;
        this.errorMessage = errorMessage;
    }
}

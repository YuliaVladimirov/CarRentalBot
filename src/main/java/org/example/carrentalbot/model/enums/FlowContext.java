package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines the current conversational context (state) the user is currently in.
 * The context serves as a security and integrity mechanism, preventing users from
 * executing commands or selecting options that are outside the scope of their
 * current workflow, thereby enforcing application flow control.
 * <p>Each context includes a human-readable label and a specific error message
 * to be displayed if an unauthorized action is attempted while in that state.</p>
 */
@Getter
public enum FlowContext {

    /**
     * The user is in the general browsing or catalog view, typically exploring
     * available car categories and checking overall car inventory.
     * Commands related to exploring cars and car details are allowed.
     */
    BROWSING_FLOW(
            "Catalog",
            """
                    This option is not available in browsing flow.
                    """),

    /**
     * The user is actively proceeding through the multistep process of creating a new booking.
     * Only commands related to completing, canceling, or navigating steps within the
     * booking wizard are permitted.
     */
    BOOKING_FLOW(
            "Booking",
            """
                    This option is not available in booking flow.
                    """),

    /**
     * The user is inside the workflow for modifying an existing, **non-CONFIRMED** booking
     * (e.g., changing phone or email). This flow is dedicated to changes made *before* the
     * final confirmation step. Only commands relevant to the modification process
     * are permitted.
     */
    EDIT_BOOKING_FLOW(
            "Edit Booking",
            """
                    This option is not available in edit flow.
                    """),

    /**
     * The user is viewing a list or detail of their past and current reservations.
     * This context allows navigation to view booking details and permits modifications
     * (e.g., changing phone or email) to any eligible reservation that has not yet
     * started.
     */
    MY_BOOKINGS_FLOW(
            "My Bookings",
            """
                    This option is not available in my bookings flow.
                    """);

    /**
     * The user-friendly, displayable name for the flow context.
     */
    private final String value;

    /**
     * The specific error message if users attempt
     * an action that is restricted within this specific flow.
     */
    private final String errorMessage;

    /**
     * Constructs a {@code FlowContext} with the specified display value and error message.
     * @param value The display name to be associated with the enum constant.
     * @param errorMessage The message to be displayed upon a flow violation.
     */
    FlowContext(String value, String errorMessage) {
        this.value = value;
        this.errorMessage = errorMessage;
    }
}

package org.example.carrentalbot.model.enums;

import lombok.Getter;

/**
 * Defines the current conversational context (state) the user is currently in.
 * The context serves as a security and integrity mechanism, preventing users from
 * executing commands or selecting options that are outside the scope of their
 * current workflow, thereby enforcing application flow control.
 */
@Getter
public enum FlowContext {

    /**
     * The user is in the general browsing or catalog view, typically exploring
     * available car categories and checking overall car inventory.
     * Commands related to exploring cars and car details are allowed.
     */
    BROWSING_FLOW  ("Catalog"),

    /**
     * The user is actively proceeding through the multistep process of creating a new booking.
     * Only commands related to completing, canceling, or navigating steps within the
     * booking wizard are permitted.
     */
    BOOKING_FLOW ("Booking"),

    /**
     * The user is inside the workflow for modifying an existing, **non-CONFIRMED** booking
     * (e.g., changing phone or email). This flow is dedicated to changes made *before* the
     * final confirmation step. Only commands relevant to the modification process
     * are permitted.
     */
    EDIT_BOOKING_FLOW ("Edit Booking"),

    /**
     * The user is viewing a list or detail of their past and current reservations.
     * This context allows navigation to view booking details and permits modifications
     * (e.g., changing phone or email) to any eligible reservation that has not yet
     * started.
     */
    MY_BOOKINGS_FLOW ("My Bookings");

    /**
     * The user-friendly, displayable name for the flow context.
     */
    private final String value;

    /**
     * Constructs a {@code FlowContext} with the specified display value.
     * @param value The display name to be associated with the enum constant.
     */
    FlowContext(String value) {
        this.value = value;
    }
}

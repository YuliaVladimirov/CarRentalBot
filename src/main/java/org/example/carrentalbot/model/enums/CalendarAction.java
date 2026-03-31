package org.example.carrentalbot.model.enums;

/**
 * Defines actions performed in the calendar interaction flow.
 * <p>Used to control navigation and selection behavior in the inline
 * calendar component.</p>
 */
public enum CalendarAction {

    /**
     * Non-actionable calendar element.
     * <p>Used for placeholders and UI elements that should not trigger
     * any business logic.</p>
     */
    IGNORE,

    /**
     * Navigates to the previous month.
     */
    PREV,

    /**
     * Navigates to the next month.
     */
    NEXT,

    /**
     * Selects a calendar date.
     * <p>Represents the completion of the date selection process.</p>
     */
    PICK
}
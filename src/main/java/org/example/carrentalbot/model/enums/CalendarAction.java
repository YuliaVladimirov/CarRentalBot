package org.example.carrentalbot.model.enums;

/**
 * Defines the specific actions that can be performed by a user interacting
 * with the inline keyboard calendar widget in the Telegram bot.
 * <p>These constants are typically used as prefixes or payloads in the callback
 * data sent when a user presses a button on the calendar interface.</p>
 */
public enum CalendarAction {
    /**
     * An action used for non-selectable items on the calendar, such as empty
     * day placeholders or the header row.
     * <p>This ensures that pressing the button sends a callback, but the system
     * is instructed to ignore the interaction without an error.</p>
     */
    IGNORE,

    /**
     * Instructs the calendar logic to decrement the month/year and render the
     * previous page (e.g., when the "Left Arrow" button is pressed).
     */
    PREV,

    /**
     * Instructs the calendar logic to increment the month/year and render the
     * next page (e.g., when the "Right Arrow" button is pressed).
     */
    NEXT,

    /**
     * Indicates that a selectable item (typically a valid day) has been chosen
     * by the user, signaling the end of the date selection process for the calendar.
     */
    PICK
}
package org.example.carrentalbot.model.enums;

/**
 * Defines the current lifecycle status of a scheduled reminder notification
 * within the system.
 * <p>This status is essential for managing the scheduling queue, retries,
 * and preventing duplicate messages.</p>
 */
public enum ReminderStatus {

    /**
     * The reminder has been created and placed into the scheduling queue,
     * but has not yet been processed or sent.
     * <p>This is the initial state for any new reminder.</p>
     */
    PENDING,

    /**
     * The reminder message has been successfully transmitted to the customer
     * via the designated communication channel (e.g., email or telegram message).
     */
    SENT,

    /**
     * The system attempted to send the reminder, but the transmission failed
     * (e.g., communication error, invalid recipient address).
     * <p>Reminders in this state may be subject to a retry policy.</p>
     */
    FAILED,

    /**
     * The reminder was explicitly revoked or invalidated before it could be sent.
     * This usually happens if the underlying booking is canceled.
     * <p>Reminders in this state will be removed from the active queue and never sent.</p>
     */
    CANCELLED
}

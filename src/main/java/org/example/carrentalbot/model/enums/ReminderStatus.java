package org.example.carrentalbot.model.enums;

/**
 * Represents the lifecycle state of a reminder notification.
 * <p>Used to track delivery progress and ensure correct handling of
 * retries, cancellations, and successful delivery.</p>
 */
public enum ReminderStatus {

    /**
     * Reminder created but not yet processed.
     * <p>This is the initial state of all reminders.</p>
     */
    PENDING,

    /**
     * Reminder successfully delivered to the customer.
     */
    SENT,

    /**
     * Reminder delivery failed.
     * <p>May be retried depending on system retry policy.</p>
     */
    FAILED,

    /**
     * Reminder invalidated before delivery.
     * <p>Typically occurs when the related booking is canceled.
     * Such reminders are not processed further.</p>
     */
    CANCELLED
}

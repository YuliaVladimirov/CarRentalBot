package org.example.carrentalbot.email;

import jakarta.mail.MessagingException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.Reminder;
import org.example.carrentalbot.model.enums.NotificationType;
import org.springframework.mail.MailException;

/**
 * Service interface for dispatching HTML-based email communications.
 * <p>Supports various notification types including booking confirmations,
 * status updates, and scheduled reminders. This service defines specific
 * recovery hooks for handling delivery failures.</p>
 */
public interface EmailService {

    /**
     * Dispatches a specific notification related to a booking event.
     * @param booking          The {@link Booking} context for the email.
     * @param notificationType The specific template and metadata to apply.
     * @throws MessagingException if the email structure is malformed.
     */
    void sendBookingNotification(Booking booking, NotificationType notificationType) throws MessagingException;

    /**
     * Recovery callback invoked after exhausted retry attempts for a notification.
     * @param exception        The root cause of the delivery failure.
     * @param booking          The booking associated with the failed attempt.
     * @param notificationType The type of notification that failed.
     */
    void recoverFailedNotification(MailException exception, Booking booking, NotificationType notificationType);

    /**
     * Dispatches a scheduled reminder regarding an upcoming or ongoing booking.
     * @param reminder The {@link Reminder} entity containing schedule and booking data.
     * @throws MessagingException if the email structure is malformed.
     */
    void sendBookingReminder(Reminder reminder) throws MessagingException;

    /**
     * Recovery callback invoked after exhausted retry attempts for a reminder.
     * @param exception The root cause of the delivery failure.
     * @param reminder  The reminder associated with the failed attempt.
     */
    void recoverFailedReminder(MailException exception, Reminder reminder);
}

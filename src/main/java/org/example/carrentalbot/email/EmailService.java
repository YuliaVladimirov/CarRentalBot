package org.example.carrentalbot.email;

import jakarta.mail.MessagingException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.Reminder;
import org.example.carrentalbot.model.enums.NotificationType;
import org.springframework.mail.MailException;

/**
 * Service interface for sending HTML email notifications.
 * Supports booking-related notifications and scheduled reminders,
 * with recovery methods for handling delivery failures.
 */
public interface EmailService {

    /**
     * Sends a booking-related notification email.
     *
     * @param booking the {@link Booking} associated with the notification
     * @param notificationType the notification type
     * @throws MessagingException if the email cannot be created
     */
    void sendBookingNotification(Booking booking, NotificationType notificationType) throws MessagingException;

    /**
     * Handles a failed booking notification after retry attempts.
     *
     * @param exception the cause of the failure
     * @param booking the related booking
     * @param notificationType the notification type
     */
    void recoverFailedNotification(MailException exception, Booking booking, NotificationType notificationType);

    /**
     * Sends a booking reminder email.
     *
     * @param reminder the {@link Reminder} containing booking data
     * @throws MessagingException if the email cannot be created
     */
    void sendBookingReminder(Reminder reminder) throws MessagingException;

    /**
     * Handles a failed reminder after retry attempts.
     *
     * @param exception the cause of the failure
     * @param reminder the related reminder
     */
    void recoverFailedReminder(MailException exception, Reminder reminder);
}

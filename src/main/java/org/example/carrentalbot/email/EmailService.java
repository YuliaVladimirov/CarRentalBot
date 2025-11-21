package org.example.carrentalbot.email;

import jakarta.mail.MessagingException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.Reminder;
import org.example.carrentalbot.model.enums.NotificationType;
import org.springframework.mail.MailException;

public interface EmailService {
    void sendBookingNotification(Booking booking, NotificationType notificationType) throws MessagingException;
    void recoverFailedNotification(MailException exception, Booking booking, NotificationType notificationType);
    void sendBookingReminder(Reminder reminder) throws MessagingException;
    void recoverFailedReminder(MailException exception, Reminder reminder);
}

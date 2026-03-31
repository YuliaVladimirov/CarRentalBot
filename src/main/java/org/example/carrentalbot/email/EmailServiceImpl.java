package org.example.carrentalbot.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.Reminder;
import org.example.carrentalbot.model.enums.NotificationType;
import org.example.carrentalbot.util.EmailTemplateBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Asynchronous implementation of {@link EmailService} with retry support.
 * Sends HTML emails using {@link JavaMailSender} and builds content via
 * {@link EmailTemplateBuilder}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    /**
     * The core Spring interface used to create and send email messages.
     * <p>Configured via {@code application.properties} to connect to the SMTP
     * server, this sender handles the low-level transmission of {@link MimeMessage}
     * objects over the network.</p>
     */
    private final JavaMailSender mailSender;

    /**
     * Component responsible for generating responsive HTML content.
     * <p>Transforms raw {@link Booking} and {@link Reminder} data into formatted
     * email bodies using predefined templates, ensuring a consistent visual
     * identity for all user-facing communications.</p>
     */
    private final EmailTemplateBuilder emailTemplateBuilder;

    /** The system email address used as the 'From' header. */
    @Value("${spring.mail.username}")
    private String userName;

    /**
     * Sends a booking notification email asynchronously.
     * Retries on {@link MailException}. If all attempts fail,
     * {@link #recoverFailedNotification(MailException, Booking, NotificationType)} is called.
     *
     * @param booking the booking associated with the notification
     * @param notificationType the notification type
     * @throws MessagingException if the email cannot be created
     */
    @Override
    @Async("emailExecutor")
    @Retryable(
            retryFor = {MailException.class},
            backoff = @Backoff(delay = 5000),
            recover = "recoverFailedNotification")
    public void sendBookingNotification(Booking booking, NotificationType notificationType) throws MessagingException {

            String htmlBody = emailTemplateBuilder.buildNotificationHtmlBody(booking, notificationType.getTitle(), notificationType.getMessage());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(booking.getEmail().trim());
            helper.setFrom(userName);
            helper.setSubject(notificationType.getSubject());
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Booking notification [{}] sent to user's email for booking {}", notificationType.name(), booking.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Recover
    public void recoverFailedNotification(MailException exception, Booking booking, NotificationType notificationType) {
        log.error("PERMANENTLY FAILED to send notification [{}] to user's email for booking {}: {}",
                notificationType.name(),
                booking.getId(),
                exception.getMessage());
    }

    /**
     * Sends a booking reminder email asynchronously.
     * Retries on {@link MailException}. If all attempts fail,
     * {@link #recoverFailedReminder(MailException, Reminder)} is called.
     *
     * @param reminder the reminder data
     * @throws MessagingException if the email cannot be created
     */
    @Override
    @Async("emailExecutor")
    @Retryable(
            retryFor = {MailException.class},
            backoff = @Backoff(delay = 5000),
            recover = "recoverFailedReminder")
    public void sendBookingReminder(Reminder reminder) throws MessagingException {

            String htmlBody = emailTemplateBuilder.buildReminderHtmlBody(reminder.getBooking(), reminder.getReminderType().getTitle(), reminder.getReminderType().getMessage());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(reminder.getBooking().getEmail().trim());
            helper.setFrom(userName);
            helper.setSubject("Booking Reminder");
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Booking reminder [{}] sent to user's email for booking {}", reminder.getReminderType().name(), reminder.getBooking().getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Recover
    public void recoverFailedReminder(MailException exception, Reminder reminder) {
        log.error("PERMANENTLY FAILED to send reminder [{}] to user's email for booking {} | {}",
                reminder.getReminderType().name(),
                reminder.getBooking().getId(),
                exception.getMessage());
    }
}

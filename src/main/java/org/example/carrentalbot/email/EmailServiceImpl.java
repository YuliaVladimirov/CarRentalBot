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

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder emailTemplateBuilder;

    @Value("${spring.mail.username}")
    private String userName;

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
            log.info("Booking notification email [{}] sent to user's email for booking {}", notificationType.name(), booking.getId());
    }

    @Override
    @Recover
    public void recoverFailedNotification(MailException exception, Booking booking, NotificationType notificationType) {
        log.error("PERMANENTLY FAILED to send notification email [{}] to user's email for booking {}: {}",
                notificationType.name(),
                booking.getId(),
                exception.getMessage());
    }

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
            log.info("Booking reminder email sent to user's email for booking {}", reminder.getBooking().getId());
    }

    @Override
    @Recover
    public void recoverFailedReminder(MailException exception, Reminder reminder) {
        log.error("PERMANENTLY FAILED to send reminder email [{}] to user's email for booking {}: {}",
                reminder.getReminderType().name(),
                reminder.getBooking().getId(),
                exception.getMessage());
    }
}

package org.example.carrentalbot.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.Reminder;
import org.example.carrentalbot.model.enums.NotificationType;
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
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${spring.mail.username}")
    private String userName;

    public EmailService(JavaMailSender mailSender,
                        EmailTemplateService emailTemplateService) {
        this.mailSender = mailSender;
        this.emailTemplateService = emailTemplateService;
    }

    @Async("emailExecutor")
    @Retryable(
            retryFor = {MailException.class},
            backoff = @Backoff(delay = 5000),
            recover = "recoverFailedNotification")
    public void sendBookingNotification(Booking booking, NotificationType notificationType) throws MessagingException {

            String htmlBody = emailTemplateService.buildNotificationHtmlBody(booking, notificationType.getTitle(), notificationType.getMessage());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(booking.getEmail().trim());
            helper.setFrom(userName);
            helper.setSubject(notificationType.getSubject());
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Booking notification email [{}] sent to {} for booking {}", notificationType.name(), booking.getEmail(), booking.getId());
    }

    @Recover
    public void recoverFailedNotification(MailException exception, Booking booking, NotificationType notificationType) {
        log.error("PERMANENTLY FAILED to send notification email [{}] to {} for booking {}: {}",
                notificationType.name(),
                booking.getEmail(),
                booking.getId(),
                exception.getMessage());
    }

    @Async("emailExecutor")
    @Retryable(
            retryFor = {MailException.class},
            backoff = @Backoff(delay = 5000),
            recover = "recoverFailedReminder")
    public void sendBookingReminder(Reminder reminder) throws MessagingException {

            String htmlBody = emailTemplateService.buildReminderHtmlBody(reminder.getBooking(), reminder.getReminderType().getTitle(), reminder.getReminderType().getMessage());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(reminder.getBooking().getEmail().trim());
            helper.setFrom(userName);
            helper.setSubject("Booking Reminder");
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Booking reminder email sent to {} for booking {}", reminder.getBooking().getEmail(), reminder.getBooking().getId());
    }

    @Recover
    public void recoverFailedReminder(MailException exception, Reminder reminder) {
        log.error("PERMANENTLY FAILED to send reminder email [{}] to {} for booking {}: {}",
                reminder.getReminderType().name(),
                reminder.getBooking().getEmail(),
                reminder.getBooking().getId(),
                exception.getMessage());
    }
}

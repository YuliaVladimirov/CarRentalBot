package org.example.carrentalbot.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.BookingNotification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    public void sendBookingNotification(Booking booking, BookingNotification bookingNotification){

        try {
            String htmlBody = emailTemplateService.buildBookingConfirmationEmail(booking, bookingNotification.getTitle(), bookingNotification.getMessage());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(booking.getEmail().trim());
            helper.setFrom(userName);
            helper.setSubject("Booking Details");
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Booking email [{}] sent to {} for booking {}", bookingNotification.name(), booking.getEmail(), booking.getId());
        } catch (Exception exception) {
            log.error("Failed to send email to {} for booking {}: {}", booking.getEmail(), booking.getId(), exception.getMessage(), exception);
        }
    }
}

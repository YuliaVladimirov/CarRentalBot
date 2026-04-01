package org.example.carrentalbot.util;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.model.Booking;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

/**
 * Builder component responsible for generating HTML email bodies
 * using Thymeleaf templates.
 * <p>This class prepares template contexts and delegates rendering
 * to {@link TemplateEngine} for different booking-related email types.</p>
 */
@Component
@RequiredArgsConstructor
public class EmailTemplateBuilder {

    /** Engine used to render email templates into HTML content. */
    private final TemplateEngine templateEngine;

    /**
     * Builds HTML email body for booking notifications.
     * <p>Includes full booking details such as car information, pricing,
     * dates, and customer contact information.</p>
     *
     * @param booking booking entity containing all relevant data
     * @param title email title displayed in template
     * @param message main message content for the email
     * @return rendered HTML email body
     */
    public String buildNotificationHtmlBody(Booking booking, String title, String message) {

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("message", message);
        context.setVariable("bookingId", booking.getId());
        context.setVariable("brand", booking.getCar().getBrand());
        context.setVariable("model", booking.getCar().getModel());
        context.setVariable("category", booking.getCar().getCategory().getValue());
        context.setVariable("startDate", booking.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        context.setVariable("endDate", booking.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        context.setVariable("totalDays", booking.getTotalDays());
        context.setVariable("dailyRate", booking.getCar().getDailyRate().setScale(0, RoundingMode.HALF_UP));
        context.setVariable("totalCost", booking.getTotalCost());
        context.setVariable("phone", booking.getPhone());
        context.setVariable("email", booking.getEmail());

        return templateEngine.process("email/notification.html", context);
    }

    /**
     * Builds HTML email body for booking reminder notifications.
     * <p>Includes essential booking details such as car information and rental period,
     * intended for reminder-style communication.</p>
     *
     * @param booking booking entity containing relevant data
     * @param title email title displayed in template
     * @param message reminder message content
     * @return rendered HTML email body
     */
    public String buildReminderHtmlBody(Booking booking, String title, String message) {

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("message", message);
        context.setVariable("bookingId", booking.getId());
        context.setVariable("startDate", booking.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        context.setVariable("endDate", booking.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        context.setVariable("totalDays", booking.getTotalDays());
        context.setVariable("brand", booking.getCar().getBrand());
        context.setVariable("model", booking.getCar().getModel());

        return templateEngine.process("email/reminder.html", context);
    }
}

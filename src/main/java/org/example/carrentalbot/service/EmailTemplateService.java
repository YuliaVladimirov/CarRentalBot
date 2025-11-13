package org.example.carrentalbot.service;

import org.example.carrentalbot.model.Booking;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplateService {

    private final TemplateEngine templateEngine;
    private final BookingService bookingService;

    public EmailTemplateService(TemplateEngine templateEngine,
                                BookingService bookingService) {
        this.templateEngine = templateEngine;
        this.bookingService = bookingService;
    }

    public String buildNotificationHtmlBody(Booking booking, String title, String message) {

        BigDecimal dailyRate = booking.getCar().getDailyRate().setScale(0, RoundingMode.HALF_UP);
        long totalDays = bookingService.calculateTotalDays(booking.getStartDate(), booking.getEndDate());

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("message", message);
        context.setVariable("bookingId", booking.getId());
        context.setVariable("brand", booking.getCar().getBrand());
        context.setVariable("model", booking.getCar().getModel());
        context.setVariable("category", booking.getCar().getCategory().getValue());
        context.setVariable("startDate", booking.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        context.setVariable("endDate", booking.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        context.setVariable("totalDays", totalDays);
        context.setVariable("dailyRate", dailyRate);
        context.setVariable("totalCost", booking.getTotalCost());
        context.setVariable("phone", booking.getPhone());
        context.setVariable("email", booking.getEmail());

        return templateEngine.process("email/notification.html", context);
    }

    public String buildReminderHtmlBody(Booking booking, String title, String message) {

        long totalDays = bookingService.calculateTotalDays(booking.getStartDate(), booking.getEndDate());

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("message", message);
        context.setVariable("bookingId", booking.getId());
        context.setVariable("startDate", booking.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        context.setVariable("endDate", booking.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        context.setVariable("totalDays", totalDays);
        context.setVariable("brand", booking.getCar().getBrand());
        context.setVariable("model", booking.getCar().getModel());

        return templateEngine.process("email/reminder.html", context);
    }
}

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

    public String buildBookingConfirmationEmail(Booking booking, String emailTitle, String emailMessage) {

        BigDecimal dailyRate = booking.getCar().getDailyRate().setScale(0, RoundingMode.HALF_UP);
        long totalDays = bookingService.calculateTotalDays(booking.getStartDate(), booking.getEndDate());

        Context context = new Context();
        context.setVariable("title", emailTitle);
        context.setVariable("message", emailMessage);
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

        return templateEngine.process("email/booking-email.html", context);
    }
}

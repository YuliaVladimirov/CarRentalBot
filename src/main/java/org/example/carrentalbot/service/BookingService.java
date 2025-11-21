package org.example.carrentalbot.service;

import org.example.carrentalbot.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    boolean isCarAvailable(UUID carId, LocalDate startDate, LocalDate endDate);
    Booking createBooking(UUID carId, Long telegramUserId,
                          LocalDate startDate, LocalDate endDate, Integer totalDays, BigDecimal totalCost,
                          String phone, String email);
    Integer calculateTotalDays(LocalDate startDate, LocalDate endDate);
    BigDecimal calculateTotalCost(BigDecimal dailyRate, long totalDays);
    List<Booking> getBookingsByCustomerTelegramId(Long telegramUserId);
    Booking getBookingById(UUID bookingId);
    Booking cancelBooking(UUID bookingId);
    Booking updateBooking(UUID bookingId, String phone, String email);
}

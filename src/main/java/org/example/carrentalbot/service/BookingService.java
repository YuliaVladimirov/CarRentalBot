package org.example.carrentalbot.service;

import org.example.carrentalbot.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing bookings and rental operations.
 */
public interface BookingService {

    /**
     * Checks whether a car is available for the given date range.
     *
     * @param carId the unique identifier of the car
     * @param startDate the start date of the rental period
     * @param endDate the end date of the rental period
     * @return {@code true} if the car is available, {@code false} otherwise
     */
    boolean isCarAvailable(UUID carId, LocalDate startDate, LocalDate endDate);

    /**
     * Creates a new booking after validating car availability for the selected period.
     *
     * @param carId the unique identifier of the car
     * @param telegramUserId the Telegram ID of the customer
     * @param startDate the start date of the rental period
     * @param endDate the end date of the rental period
     * @param totalDays the total number of rental days
     * @param totalCost the total booking cost
     * @param phone the customer's phone number
     * @param email the customer's email address
     * @return the created {@link Booking} entity
     */
    Booking createBooking(UUID carId, Long telegramUserId,
                          LocalDate startDate, LocalDate endDate, Integer totalDays, BigDecimal totalCost,
                          String phone, String email);

    /**
     * Calculates the total number of days for a rental period (inclusive).
     *
     * @param startDate the first day of the rental
     * @param endDate the last day of the rental
     * @return the total number of days
     */
    Integer calculateTotalDays(LocalDate startDate, LocalDate endDate);

    /**
     * Calculates the total rental cost based on daily rate and duration.
     *
     * @param dailyRate the price per day
     * @param totalDays the number of rental days
     * @return the total cost of the rental
     */
    BigDecimal calculateTotalCost(BigDecimal dailyRate, long totalDays);

    /**
     * Retrieves all bookings for a given Telegram user.
     *
     * @param telegramUserId the customer's Telegram ID
     * @return a list of {@link Booking} entities
     */
    List<Booking> getBookingsByCustomerTelegramId(Long telegramUserId);

    /**
     * Retrieves a booking by its unique ID.
     *
     * @param bookingId the unique identifier of the booking
     * @return the {@link Booking} entity
     */
    Booking getBookingById(UUID bookingId);

    /**
     * Cancels an existing booking.
     *
     * @param bookingId the unique identifier of the booking
     * @return the updated {@link Booking} entity
     */
    Booking cancelBooking(UUID bookingId);

    /**
     * Updates contact information for an existing booking.
     *
     * @param bookingId the unique identifier of the booking
     * @param phone the new phone number (optional)
     * @param email the new email address (optional)
     * @return the updated {@link Booking} entity
     */
    Booking updateBooking(UUID bookingId, String phone, String email);
}

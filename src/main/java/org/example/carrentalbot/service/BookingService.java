package org.example.carrentalbot.service;

import org.example.carrentalbot.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing vehicle reservations and rental calculations.
 * <p>Orchestrates the lifecycle of a {@link Booking}, including availability
 * verification, cost estimation, and state transitions (creation, updates, and cancellations).</p>
 */
public interface BookingService {

    /**
     * Checks if a specific vehicle is free of reservations for the requested period.
     * @param carId     The unique identifier of the vehicle.
     * @param startDate The start date of the intended rental.
     * @param endDate   The end date of the intended rental.
     * @return {@code true} if no overlapping bookings exist; {@code false} otherwise.
     */
    boolean isCarAvailable(UUID carId, LocalDate startDate, LocalDate endDate);

    /**
     * Persists a new reservation after validating car status and availability.
     * @param carId          The unique identifier of the vehicle to be reserved.
     * @param telegramUserId The unique Telegram ID of the customer making the reservation.
     * @param startDate      The start date (inclusive) of the intended rental period.
     * @param endDate        The end date (inclusive) of the intended rental period.
     * @param totalDays      The pre-calculated duration of the rental in days.
     * @param totalCost      The total monetary value of the reservation, including all applicable rates.
     * @param phone          The primary contact phone number provided by the customer for this booking.
     * @param email          The contact email address provided by the customer for reservation details.
     * @return The confirmed {@link Booking} entity after successful persistence.
     */
    Booking createBooking(UUID carId, Long telegramUserId,
                          LocalDate startDate, LocalDate endDate, Integer totalDays, BigDecimal totalCost,
                          String phone, String email);

    /**
     * Calculates the inclusive number of days for a rental period.
     * @param startDate The first day of rental.
     * @param endDate   The last day of rental.
     * @return The total count of days (inclusive).
     */
    Integer calculateTotalDays(LocalDate startDate, LocalDate endDate);

    /**
     * Computes the total rental price based on a daily rate.
     * @param dailyRate The price per single day.
     * @param totalDays The duration of the rental.
     * @return The calculated cost, rounded to two decimal places.
     */
    BigDecimal calculateTotalCost(BigDecimal dailyRate, long totalDays);

    /**
     * Retrieves all reservations associated with a specific Telegram user.
     * @param telegramUserId The user's unique Telegram ID.
     * @return A list of bookings for the customer.
     */
    List<Booking> getBookingsByCustomerTelegramId(Long telegramUserId);

    /**
     * Retrieves a specific reservation including associated car details.
     * @param bookingId The unique identifier of the booking.
     * @return The found {@link Booking}.
     */
    Booking getBookingById(UUID bookingId);

    /**
     * Cancels an existing reservation by changing the status to "Canceled".
     * @param bookingId The ID of the reservation to terminate.
     * @return The updated booking entity.
     */
    Booking cancelBooking(UUID bookingId);

    /**
     * Updates contact information for an existing booking.
     * @param bookingId The ID of the reservation to modify.
     * @param phone     The new phone number (optional).
     * @param email     The new email address (optional).
     * @return The updated booking entity.
     */
    Booking updateBooking(UUID bookingId, String phone, String email);
}

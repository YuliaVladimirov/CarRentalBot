package org.example.carrentalbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidStateException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.model.enums.BookingStatus;
import org.example.carrentalbot.model.enums.CarStatus;
import org.example.carrentalbot.repository.BookingRepository;
import org.example.carrentalbot.repository.CarRepository;
import org.example.carrentalbot.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link BookingService}.
 * <p>Orchestrates the lifecycle of a {@link Booking}, including availability
 * verification, cost estimation, and state transitions (creation, updates, and cancellations).</p>
 * <p>This service coordinates between {@link BookingRepository}, {@link CarRepository},
 * and {@link CustomerRepository} to ensure atomic operations when creating or
 * modifying reservations.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    /**
     * Verifies if a specific vehicle is free of reservations for the requested period.
     * @param carId     The unique identifier of the vehicle.
     * @param startDate The start date of the intended rental.
     * @param endDate   The end date of the intended rental.
     * @return {@code true} if no overlapping bookings exist; {@code false} otherwise.
     */
    @Override
    public boolean isCarAvailable(UUID carId, LocalDate startDate, LocalDate endDate) {
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(carId, startDate, endDate);
        return !hasOverlap;
    }

    /**
     * Executes a transactional booking creation flow.
     * <ol>
     * <li>Validates user and car existence.</li>
     * <li>Enforces administrative {@link CarStatus} checks.</li>
     * <li>Performs a final concurrency check on date availability.</li>
     * <li>Persists the confirmed reservation.</li>
     * </ol>
     * @param carId          The unique identifier of the vehicle to be reserved.
     * @param telegramUserId The unique Telegram ID of the customer making the reservation.
     * @param startDate      The start date (inclusive) of the intended rental period.
     * @param endDate        The end date (inclusive) of the intended rental period.
     * @param totalDays      The pre-calculated duration of the rental in days.
     * @param totalCost      The total monetary value of the reservation, including all applicable rates.
     * @param phone          The primary contact phone number provided by the customer for this booking.
     * @param email          The contact email address provided by the customer for reservation details.
     * @return The confirmed {@link Booking} entity after successful persistence.
     * @throws DataNotFoundException if the car or user does not exist.
     * @throws InvalidStateException if the car is not in service or is already booked.
     */
    @Override
    @Transactional
    public Booking createBooking(UUID carId, Long telegramUserId,
                                 LocalDate startDate, LocalDate endDate, Integer totalDays, BigDecimal totalCost,
                                 String phone, String email) {

        log.debug("Fetching customer: telegram user id={}", telegramUserId);
        Customer customer = customerRepository.findByTelegramUserId(telegramUserId).orElseThrow(() -> new DataNotFoundException(String.format("User with id: %s, was not found.", telegramUserId)));

        log.debug("Fetching car: car id={}", carId);
        Car car = carRepository.findById(carId).orElseThrow(() -> new DataNotFoundException(String.format("Car with id: %s, was not found.", carId)));

        if (car.getCarStatus() != CarStatus.IN_SERVICE) {
            throw new InvalidStateException(String.format("Car is not available for booking due to administrative status: %s", car.getCarStatus().getValue()));
        }

        if (!isCarAvailable(carId, startDate, endDate)) {
            throw new InvalidStateException("Car is no longer available for selected dates");
        }

        log.debug("Creating new booking");
        Booking booking = Booking.builder()
                .customer(customer)
                .car(car)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .totalCost(totalCost)
                .phone(phone)
                .email(email)
                .status(BookingStatus.CONFIRMED)
                .build();

        log.debug("Saving new booking");
        return bookingRepository.saveAndFlush(booking);
    }

    /**
     * Calculates the inclusive number of days for a rental period.
     * @param startDate The first day of rental.
     * @param endDate   The last day of rental.
     * @return The total count of days (inclusive).
     */
    @Override
    public Integer calculateTotalDays(LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating total days between {} and {}", startDate, endDate);
        return Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate) + 1);
    }

    /**
     * Computes the total rental price based on a daily rate.
     * @param dailyRate The price per single day.
     * @param totalDays The duration of the rental.
     * @return The calculated cost, rounded to two decimal places.
     */
    @Override
    public BigDecimal calculateTotalCost(BigDecimal dailyRate, long totalDays) {
        log.debug("Calculating total cost: dailyRate={}, totalDays={}", dailyRate, totalDays);
        return dailyRate.multiply(BigDecimal.valueOf(totalDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Retrieves all reservations associated with a specific Telegram user.
     * @param telegramUserId The user's unique Telegram ID.
     * @return A list of bookings for the customer.
     * @throws DataNotFoundException if the customer was not found in the database.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByCustomerTelegramId(Long telegramUserId) {
        log.debug("Fetching customer: telegram user id={}", telegramUserId);
        Customer customer = customerRepository.findByTelegramUserId(telegramUserId).orElseThrow(() -> new DataNotFoundException(String.format("Customer with id: %s, was not found.", telegramUserId)));

        log.debug("Fetching bookings for customer: telegram user id={}", telegramUserId);
        return bookingRepository.findByCustomerId(customer.getId());
    }

    /**
     * Fetches a specific reservation including associated car details.
     * @param bookingId The unique identifier of the booking.
     * @return The found {@link Booking}.
     * @throws DataNotFoundException if the booking was not found in the database.
     */
    @Override
    @Transactional(readOnly = true)
    public Booking getBookingById(UUID bookingId) {
        log.debug("Fetching booking: booking id={}", bookingId);
        return bookingRepository.findByIdWithCar(bookingId).orElseThrow(() -> new DataNotFoundException(String.format("Booking with id: %s, was not found.", bookingId)));
    }

    /**
     * Transitions a booking status to {@link BookingStatus#CANCELLED}.
     * @param bookingId The ID of the reservation to terminate.
     * @return The updated booking entity.
     * @throws DataNotFoundException if the booking was not found in the database.
     */
    @Override
    @Transactional
    public Booking cancelBooking(UUID bookingId) {
        log.debug("Fetching booking: booking id={}", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new DataNotFoundException(String.format("Booking with id: %s, was not found.", bookingId)));

        log.debug("Marking booking as CANCELLED: booking id={}", bookingId);
        booking.setStatus(BookingStatus.CANCELLED);

        log.debug("Saving canceled booking: booking id={}", bookingId);
        bookingRepository.saveAndFlush(booking);

        log.debug("Fetching canceled booking: booking id={}", bookingId);
        return bookingRepository.findByIdWithCar(bookingId)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Booking with id: %s, was not found after update.", bookingId)
                ));
    }

    /**
     * Updates contact information for an existing reservation.
     * @param bookingId The ID of the reservation to modify.
     * @param phone     The new phone number (optional).
     * @param email     The new email address (optional).
     * @return The updated booking entity.
     * @throws DataNotFoundException if the booking was not found in the database.
     */
    @Override
    @Transactional
    public Booking updateBooking(UUID bookingId, String phone, String email) {
        log.debug("Fetching booking: booking id={}", bookingId);
        Booking existingBooking = bookingRepository.findById(bookingId).orElseThrow(() -> new DataNotFoundException(String.format("Booking with id: %s, was not found.", bookingId)));

        log.debug("Updating booking: booking id={}", bookingId);
        Optional.ofNullable(phone).ifPresent(existingBooking::setPhone);
        Optional.ofNullable(email).ifPresent(existingBooking::setEmail);

        log.debug("Saving updated booking: booking id={}", bookingId);
        bookingRepository.saveAndFlush(existingBooking);

        log.debug("Fetching updated booking: booking id={}", bookingId);
        return bookingRepository.findByIdWithCar(bookingId)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Booking with id: %s, was not found after update.", bookingId)
                ));
    }
}

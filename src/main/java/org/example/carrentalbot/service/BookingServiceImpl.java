package org.example.carrentalbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidStateException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.model.enums.BookingStatus;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    @Override
    public boolean isCarAvailable(UUID carId, LocalDate startDate, LocalDate endDate) {
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(carId, startDate, endDate);
        return !hasOverlap;
    }

    @Override
    @Transactional
    public Booking createBooking(UUID carId, Long telegramUserId,
                                 LocalDate startDate, LocalDate endDate, Integer totalDays, BigDecimal totalCost,
                                 String phone, String email) {

        log.debug("Fetching customer: telegram user id={}", telegramUserId);
        Customer customer = customerRepository.findByTelegramUserId(telegramUserId).orElseThrow(() -> new DataNotFoundException(String.format("User with id: %s, was not found.", telegramUserId)));

        log.debug("Fetching car: car id={}", carId);
        Car car = carRepository.findById(carId).orElseThrow(() -> new DataNotFoundException(String.format("Car with id: %s, was not found.", carId)));

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

    @Override
    public Integer calculateTotalDays(LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating total days between {} and {}", startDate, endDate);
        return Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate) + 1);
    }

    @Override
    public BigDecimal calculateTotalCost(BigDecimal dailyRate, long totalDays) {
        log.debug("Calculating total cost: dailyRate={}, totalDays={}", dailyRate, totalDays);
        return dailyRate.multiply(BigDecimal.valueOf(totalDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByCustomerTelegramId(Long telegramUserId) {
        log.debug("Fetching customer: telegram user id={}", telegramUserId);
        Customer customer = customerRepository.findByTelegramUserId(telegramUserId).orElseThrow(() -> new DataNotFoundException(String.format("Customer with id: %s, was not found.", telegramUserId)));

        log.debug("Fetching bookings for customer: telegram user id={}", telegramUserId);
        return bookingRepository.findByCustomerId(customer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingById(UUID bookingId) {
        log.debug("Fetching booking: booking id={}", bookingId);
        return bookingRepository.findByIdWithCar(bookingId).orElseThrow(() -> new DataNotFoundException(String.format("Booking with id: %s, was not found.", bookingId)));
    }

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

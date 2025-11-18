package org.example.carrentalbot.service;

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

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    public BookingService(BookingRepository bookingRepository, CarRepository carRepository, CustomerRepository customerRepository) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
    }

    public boolean isCarAvailable(UUID carId, LocalDate startDate, LocalDate endDate) {
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(carId, startDate, endDate);
        return !hasOverlap;
    }

    @Transactional
    public Booking createBooking(UUID carId, Long telegramUserId,
                                 LocalDate startDate, LocalDate endDate, Integer totalDays, BigDecimal totalCost,
                                 String phone, String email) {

        Customer customer = customerRepository.findByTelegramUserId(telegramUserId).orElseThrow(() -> new DataNotFoundException(String.format("User with id: %s, was not found.", telegramUserId)));

        Car car = carRepository.findById(carId).orElseThrow(() -> new DataNotFoundException(String.format("Car with id: %s, was not found.", carId)));

        if (!isCarAvailable(carId, startDate, endDate)) {
            throw new InvalidStateException("Car is no longer available for selected dates");
        }

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

        return bookingRepository.saveAndFlush(booking);
    }

    public Integer calculateTotalDays(LocalDate startDate, LocalDate endDate) {
        return Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate) + 1);
    }

    public BigDecimal calculateTotalCost(BigDecimal dailyRate, long totalDays) {
        return dailyRate.multiply(BigDecimal.valueOf(totalDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByCustomerTelegramId(Long telegramUserId) {
        Customer customer = customerRepository.findByTelegramUserId(telegramUserId).orElseThrow(() -> new DataNotFoundException(String.format("Customer with id: %s, was not found.", telegramUserId)));
        return bookingRepository.findByCustomerId(customer.getId());
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(UUID bookingId) {
        return bookingRepository.findByIdWithCar(bookingId).orElseThrow(() -> new DataNotFoundException(String.format("Booking with id: %s, was not found.", bookingId)));
    }

    @Transactional
    public Booking cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new DataNotFoundException(String.format("Booking with id: %s, was not found.", bookingId)));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.saveAndFlush(booking);

        return bookingRepository.findByIdWithCar(bookingId)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Booking with id: %s, was not found after update.", bookingId)
                ));
    }

    @Transactional
    public Booking updateBooking(UUID bookingId, String phone, String email) {
        Booking existingBooking = bookingRepository.findById(bookingId).orElseThrow(() -> new DataNotFoundException(String.format("Booking with id: %s, was not found.", bookingId)));
        Optional.ofNullable(phone).ifPresent(existingBooking::setPhone);
        Optional.ofNullable(email).ifPresent(existingBooking::setEmail);

        bookingRepository.saveAndFlush(existingBooking);

        return bookingRepository.findByIdWithCar(bookingId)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Booking with id: %s, was not found after update.", bookingId)
                ));
    }
}

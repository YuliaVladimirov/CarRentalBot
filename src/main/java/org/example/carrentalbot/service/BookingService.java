package org.example.carrentalbot.service;

import org.example.carrentalbot.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public boolean isCarAvailable(UUID carId, LocalDate startDate, LocalDate endDate) {
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(carId, startDate, endDate);
        return !hasOverlap;
    }
}

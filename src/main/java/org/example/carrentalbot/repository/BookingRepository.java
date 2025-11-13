package org.example.carrentalbot.repository;

import org.example.carrentalbot.model.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query(value = """
                SELECT COUNT(b) > 0
                FROM Booking b
                WHERE b.car.id = :carId
                  AND b.status = org.example.carrentalbot.model.enums.BookingStatus.CONFIRMED
                  AND b.startDate <= :endDate
                  AND b.endDate >= :startDate
            """)
    boolean existsOverlappingBooking(
            @Param("carId") UUID carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @EntityGraph(attributePaths = {"car"})
    List<Booking> findByCustomerId(UUID id);

    @EntityGraph(attributePaths = {"car"})
    @Query(value = """
            SELECT b FROM Booking b WHERE b.id = :id
            """)
    Optional<Booking> findByIdWithCar(@Param("id") UUID id);
}

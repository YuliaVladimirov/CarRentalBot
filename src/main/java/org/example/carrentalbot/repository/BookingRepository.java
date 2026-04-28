package org.example.carrentalbot.repository;

import org.example.carrentalbot.model.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing {@link Booking} persistence operations.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Checks whether a car has an overlapping confirmed booking within the given period.
     * <p>An overlap exists if an existing booking:
     * <ul>
     *   <li>belongs to the same car</li>
     *   <li>has status CONFIRMED</li>
     *   <li>intersects with the given date range</li>
     * </ul>
     * </p>
     *
     * @param carId the car identifier
     * @param startDate start of the requested period
     * @param endDate end of the requested period
     * @return {@code true} if a conflicting booking exists
     */
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

    /**
     * Retrieves all bookings for a given customer.
     * <p>Fetches associated car data eagerly.</p>
     *
     * @param id the customer identifier
     * @return list of {@link Booking} entities for the customer
     */
    @EntityGraph(attributePaths = {"car"})
    List<Booking> findByCustomerId(UUID id);


    /**
     * Retrieves a booking by its identifier including associated car data.
     *
     * @param id the booking identifier
     * @return an {@link Optional} containing the {@link Booking} if found
     */
    @EntityGraph(attributePaths = {"car"})
    @Query(value = """
            SELECT b FROM Booking b WHERE b.id = :id
            """)
    Optional<Booking> findByIdWithCar(@Param("id") UUID id);
}

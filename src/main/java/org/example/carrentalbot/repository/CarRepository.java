package org.example.carrentalbot.repository;

import org.example.carrentalbot.record.CarProjection;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing {@link Car} persistence operations.
 */
@Repository
public interface CarRepository extends JpaRepository<Car, UUID> {

    /**
     * Retrieves car categories along with the minimum daily rate among cars in each category.
     *
     * @return a list of {@link CarProjection} containing category and minimum price
     */
    @Query(value = """
            SELECT new org.example.carrentalbot.record.CarProjection(c.category, MIN(c.dailyRate))
            FROM Car c
            GROUP BY c.category
            """)
    List<CarProjection> getCarCategories();

    /**
     * Finds all cars in the given category that are currently in service.
     *
     * @param category the car category to filter by
     * @return list of available {@link Car} entities in the specified category
     */
    @Query(value = """
            SELECT c FROM Car c
            WHERE c.category = :category
            AND c.carStatus = org.example.carrentalbot.model.enums.CarStatus.IN_SERVICE
""")
    List<Car> findByCategory(
            @Param("category") CarCategory category);

    /**
     * Finds all available cars for a given category and date range.
     * <p>A car is considered available if:
     * <ul>
     *   <li>it is in service</li>
     *   <li>it does not have a confirmed booking overlapping the given period</li>
     * </ul>
     * </p>
     *
     * @param category the car category to filter by
     * @param startDate rental start date
     * @param endDate rental end date
     * @return list of available {@link Car} entities
     */
    @Query(value = """
            SELECT c FROM Car c
            WHERE c.category = :category
            AND c.carStatus = org.example.carrentalbot.model.enums.CarStatus.IN_SERVICE
            AND c.id NOT IN (
            SELECT b.car.id FROM Booking b
            WHERE b.startDate <= :endDate
            AND b.endDate >= :startDate
            AND b.status = org.example.carrentalbot.model.enums.BookingStatus.CONFIRMED)
            """)
    List<Car> findAvailableCarsByCategoryAndDates(
            @Param("category") CarCategory category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

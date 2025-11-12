package org.example.carrentalbot.repository;

import org.example.carrentalbot.dto.CarProjectionDto;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {

    @Query("""
            SELECT new org.example.carrentalbot.dto.CarProjectionDto(c.category, MIN(c.dailyRate))
            FROM Car c
            GROUP BY c.category
            """)
    List<CarProjectionDto> getCarCategories();

    List<Car> findByCategory(CarCategory carCategory);

    @Query("""
            SELECT c FROM Car c
            WHERE c.category = :category
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

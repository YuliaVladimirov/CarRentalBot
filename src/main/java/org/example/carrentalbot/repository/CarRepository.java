package org.example.carrentalbot.repository;

import org.example.carrentalbot.dto.CarProjectionDto;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {

    @Query("""
            SELECT new org.example.carrentalbot.dto.CarProjectionDto(
                        c.category, MIN(c.dailyRate))
                        FROM Car c
                        GROUP BY c.category
            """)
    List<CarProjectionDto> getCarCategories();

    List<Car> findByCategory(CarCategory carCategory);
}

package org.example.carrentalbot.repository;

import org.example.carrentalbot.dto.CategoryAvailabilityDto;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {

    @Query("SELECT new org.example.carrentalbot.dto.CategoryAvailabilityDto(c.category, COUNT(c)) " +
            "FROM Car c WHERE c.available = true GROUP BY c.category")
    List<CategoryAvailabilityDto> countAvailableCarsByCategory();

    List<Car> findByCategoryAndAvailableIs(CarCategory carCategory, Boolean available);
}

package org.example.carrentalbot.service;

import org.example.carrentalbot.record.CarProjection;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing cars and availability within the bot.
 */
public interface CarService {

    /**
     * Retrieves car categories with the minimal daily rate among cars in each category.
     *
     * @return a list of {@link CarProjection} containing the {@link CarCategory}
     *         and the minimal {@link BigDecimal} daily rate
     */
    List<CarProjection> getCarCategories();

    /**
     * Retrieves all cars for the specified category.
     *
     * @param carCategory the category to filter by
     * @return a list of {@link Car} entities matching the category
     */
    List<Car> getAllCarsByCategory(CarCategory carCategory);

    /**
     * Retrieves a car by its unique ID.
     *
     * @param carId the {@link UUID} of the car
     * @return the {@link Car} entity
     */
    Car getCar(UUID carId);

    /**
     * Retrieves available cars for the given category within the specified date range.
     *
     * @param carCategory the desired {@link CarCategory}
     * @param startDate the pick-up date
     * @param endDate the return date
     * @return a list of available {@link Car} entities
     */
    List<Car> getAvailableCarsByCategoryAndDates(CarCategory carCategory, LocalDate startDate, LocalDate endDate);
}

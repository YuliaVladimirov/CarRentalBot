package org.example.carrentalbot.service;

import org.example.carrentalbot.record.CarProjection;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing the vehicle fleet and querying availability.
 * <p>Provides a comprehensive set of operations for vehicle discovery,
 * detailed fleet inspection, and temporal availability filtering.</p>
 */
public interface CarService {

    /**
     * Retrieves a summarized list of all unique car categories.
     * @return A list of {@link CarProjection} objects.
     */
    List<CarProjection> getCarCategories();

    /**
     * Retrieves all vehicles belonging to the specified category.
     * @param carCategory The category to filter by.
     * @return A list of {@link Car} entities matching the category.
     */
    List<Car> getAllCarsByCategory(CarCategory carCategory);

    /**
     * Retrieves a single car by its unique ID.
     * @param carId The UUID of the vehicle.
     * @return The found {@link Car} entity.
     */
    Car getCar(UUID carId);

    /**
     * Queries the list of vehicles that are unreserved and ready for rental
     * within a specific timeframe.
     * <p>This method performs a temporal intersection check to ensure the
     * returned vehicles have no booking conflicts between the start and end dates.</p>
     * @param carCategory The desired {@link CarCategory}.
     * @param startDate   The intended pick-up date.
     * @param endDate     The intended return date.
     * @return A list of available {@link Car} entities.
     */
    List<Car> getAvailableCarsByCategoryAndDates(CarCategory carCategory, LocalDate startDate, LocalDate endDate);
}

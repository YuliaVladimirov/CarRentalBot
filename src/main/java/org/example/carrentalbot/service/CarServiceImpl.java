package org.example.carrentalbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.record.CarProjection;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.repository.CarRepository;
import org.example.carrentalbot.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link CarService}
 * <p>Provides a comprehensive set of operations for vehicle discovery,
 * detailed fleet inspection, and temporal availability filtering.</p>
 * <p>Uses {@link CustomerRepository} to perform optimized database queries.
 * This implementation delegates complex filtering—such as date-based
 * availability—directly to the persistence layer to minimize in-memory
 * processing of the data.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    /**
     * Fetches a summarized view of car categories via a custom repository projection.
     * @return A list of {@link CarProjection} containing category types
     * and their respective entry-level daily rates
     */
    @Override
    public List<CarProjection> getCarCategories() {
        log.debug("Fetching car categories");
        return carRepository.getCarCategories();
    }


    /**
     * Retrieves all vehicles associated with a specific category,
     * regardless of current availability.
     * @param carCategory The {@link CarCategory} to filter by.
     * @return A list of {@link Car} entities belonging to the specified group.
     */
    @Override
    public List<Car> getAllCarsByCategory(CarCategory carCategory) {
        log.debug("Fetching all cars for category: car category={}", carCategory);
        return carRepository.findByCategory(carCategory);
    }

    /**
     * Fetches a specific vehicle by its unique identifier.
     * @param carId The {@link UUID} of the car.
     * @return The {@link Car} entity.
     * @throws DataNotFoundException if no vehicle exists with the provided ID.
     */
    @Override
    public Car getCar(UUID carId) {
        log.debug("Fetching car: car id={}", carId);
        return carRepository.findById(carId).orElseThrow(() -> new DataNotFoundException(String.format("Car with id: %s, was not found.", carId)));
    }

    /**
     * Executes a temporal availability query to find cars without booking conflicts.
     * <p>This method relies on the repository to perform an intersection check
     * against existing reservations for the requested date range.</p>
     * @param carCategory The category to search within.
     * @param startDate   The start of the rental period.
     * @param endDate     The end of the rental period.
     * @return A list of available {@link Car} entities.
     */
    @Override
    public List<Car> getAvailableCarsByCategoryAndDates(CarCategory carCategory, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching available cars between {} and {} for category: category={}",
                carCategory, startDate, endDate);
        return carRepository.findAvailableCarsByCategoryAndDates(carCategory, startDate, endDate);
    }
}

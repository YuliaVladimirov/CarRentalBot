package org.example.carrentalbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.record.CarProjection;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    @Override
    public List<CarProjection> getCarCategories() {
        log.debug("Fetching car categories");
        return carRepository.getCarCategories();
    }

    @Override
    public List<Car> getAllCarsByCategory(CarCategory carCategory) {
        log.debug("Fetching all cars for category: car category={}", carCategory);
        return carRepository.findByCategory(carCategory);
    }

    @Override
    public Car getCar(UUID carId) {
        log.debug("Fetching car: car id={}", carId);
        return carRepository.findById(carId).orElseThrow(() -> new DataNotFoundException(String.format("Car with id: %s, was not found.", carId)));
    }

    @Override
    public List<Car> getAvailableCarsByCategoryAndDates(CarCategory carCategory, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching available cars between {} and {} for category: category={}",
                carCategory, startDate, endDate);
        return carRepository.findAvailableCarsByCategoryAndDates(carCategory, startDate, endDate);
    }
}

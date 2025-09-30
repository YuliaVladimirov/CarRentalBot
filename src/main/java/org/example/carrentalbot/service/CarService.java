package org.example.carrentalbot.service;

import org.example.carrentalbot.dto.CategoryAvailabilityDto;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<CategoryAvailabilityDto> getAvailableCarCounts() {
        return carRepository.countAvailableCarsByCategory();
    }

    public List<Car> getCarsByCategory(CarCategory carCategory) {
        return carRepository.findByCategoryAndAvailableIs(carCategory, true);
    }

    public Optional<Car> getCarInfo(UUID carId) {
        return carRepository.findById(carId);
    }
}

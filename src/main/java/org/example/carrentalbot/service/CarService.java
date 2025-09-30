package org.example.carrentalbot.service;

import org.example.carrentalbot.dto.CategoryAvailabilityDto;
import org.example.carrentalbot.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<CategoryAvailabilityDto> getAvailableCarCounts() {
        return carRepository.countAvailableCarsByCategory();
    }
}

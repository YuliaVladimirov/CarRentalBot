package org.example.carrentalbot.service;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.CarProjectionDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    @Override
    public List<CarProjectionDto> getCarCategories() {
        return carRepository.getCarCategories();
    }

    @Override
    public List<Car> getAllCarsByCategory(CarCategory carCategory) {
        return carRepository.findByCategory(carCategory);
    }

    @Override
    public Car getCar(UUID carId) {
        return carRepository.findById(carId).orElseThrow(() -> new DataNotFoundException(String.format("Car with id: %s, was not found.", carId)));
    }

    @Override
    public List<Car> getAvailableCarsByCategoryAndDates(CarCategory carCategory, LocalDate startDate, LocalDate endDate) {
        return carRepository.findAvailableCarsByCategoryAndDates(carCategory, startDate, endDate);
    }
}

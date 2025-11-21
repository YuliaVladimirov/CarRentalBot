package org.example.carrentalbot.service;

import org.example.carrentalbot.record.CarProjectionDto;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CarService {
    List<CarProjectionDto> getCarCategories();
    List<Car> getAllCarsByCategory(CarCategory carCategory);
    Car getCar(UUID carId);
    List<Car> getAvailableCarsByCategoryAndDates(CarCategory carCategory, LocalDate startDate, LocalDate endDate);
}

package org.example.carrentalbot.dto;

import org.example.carrentalbot.model.enums.CarCategory;

public record CategoryAvailabilityDto(CarCategory category, Long count) {}

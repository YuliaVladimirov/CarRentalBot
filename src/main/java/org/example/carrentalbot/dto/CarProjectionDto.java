package org.example.carrentalbot.dto;

import org.example.carrentalbot.model.enums.CarCategory;

import java.math.BigDecimal;

public record CarProjectionDto(CarCategory category, BigDecimal minimalDailyRate) {}

package org.example.carrentalbot.record;

import org.example.carrentalbot.model.enums.CarCategory;
import java.math.BigDecimal;

public record CarProjection(CarCategory category, BigDecimal minimalDailyRate) {}

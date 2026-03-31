package org.example.carrentalbot.record;

import org.example.carrentalbot.model.enums.CarCategory;
import java.math.BigDecimal;

/**
 * Represents a summary of available cars grouped by category with pricing information.
 * <p>Used to present users with an overview of available vehicle categories
 * and their starting rental prices.</p>
 *
 * @param category car category (e.g., SEDAN, SUV, LUXURY)
 * @param minimalDailyRate lowest available daily rental price in this category
 */
public record CarProjection(CarCategory category, BigDecimal minimalDailyRate) {}

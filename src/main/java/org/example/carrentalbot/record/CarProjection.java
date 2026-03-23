package org.example.carrentalbot.record;

import org.example.carrentalbot.model.enums.CarCategory;
import java.math.BigDecimal;

/**
 * A data transfer object (DTO) representing a summary of car availability by category.
 * <p>This projection is primarily used in the discovery phase of the rental flow
 * to provide users with a high-level overview of vehicle options and their
 * associated entry-level pricing.</p>
 * @param category The specific {@link CarCategory} (e.g., SEDAN, SUV, LUXURY)
 * grouping the vehicles.
 * @param minimalDailyRate The lowest daily rental price available within this
 * category, serving as a "starting at" price point.
 */
public record CarProjection(CarCategory category, BigDecimal minimalDailyRate) {}

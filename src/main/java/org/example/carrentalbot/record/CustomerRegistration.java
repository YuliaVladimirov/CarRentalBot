package org.example.carrentalbot.record;

import org.example.carrentalbot.model.Customer;

/**
 * A data transfer object (DTO) representing the outcome of a customer registration attempt.
 * <p>This record acts as a carrier for both the persisted {@link Customer} entity
 * and a flag indicating the nature of the database operation (Creation vs. Retrieval).
 * This allows calling handlers to differentiate their UI response based on the
 * user's history with the bot.</p>
 * * @param customer The {@link Customer} entity, either newly created or
 * retrieved from the existing database record.
 * @param isNew    A boolean flag set to {@code true} if a new record was
 * inserted during this transaction; {@code false} if the
 * customer was already known to the system.
 */
public record CustomerRegistration(Customer customer, boolean isNew) {}

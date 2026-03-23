package org.example.carrentalbot.service;

import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.record.CustomerRegistration;

/**
 * Service interface for managing customer identity and registration within the bot.
 * <p>Handles the lifecycle of a {@link Customer} entity, ensuring that every
 * Telegram user interacting with the bot is correctly mapped to a persistent
 * database record.</p>
 */
public interface CustomerService {

    /**
     * Ensures that a customer record exists in the database for the given Telegram user.
     * <p>It searches for an existing record by the Telegram User ID.
     * If found, it returns the existing data; otherwise, it persists a new customer record.</p>
     * @param chatId       The unique identifier for the current chat session.
     * @param telegramUser The data transfer object containing user details from Telegram.
     * @return A {@link CustomerRegistration} containing the customer entity and a
     * flag indicating if the account was just created.
     */
    CustomerRegistration registerIfNotExists(Long chatId, FromDto telegramUser);
}

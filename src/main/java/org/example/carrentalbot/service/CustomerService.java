package org.example.carrentalbot.service;

import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.record.CustomerRegistration;

/**
 * Service interface for managing customer identity and registration within the bot.
 */
public interface CustomerService {

    /**
     * Registers a new customer if one does not already exist (idempotent operation).
     *
     * @param chatId the unique identifier of the current chat session provided by the Telegram API
     * @param telegramUser the data transfer object containing user details from Telegram
     * @return a {@link CustomerRegistration} containing the {@link Customer} entity and
     *         a flag indicating whether a new record was created
     */
    CustomerRegistration registerIfNotExists(Long chatId, FromDto telegramUser);
}

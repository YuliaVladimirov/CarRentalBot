package org.example.carrentalbot.session;

import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.model.enums.FlowContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing user session data across distributed instances.
 * Provides a type-safe API for storing and retrieving temporary data
 * associated with a chat session, enabling multistep conversational flows
 * in a stateless environment.
 */
public interface SessionService {

    /**
     * Stores a value in the session under the specified field.
     *
     * @param chatId the chat session identifier
     * @param field the session key
     * @param value the value to store (must be serializable)
     */
    void put(Long chatId, String field, Object value);

    /**
     * Retrieves a String value from the session.
     *
     * @param chatId the chat session identifier
     * @param field the session key
     * @return the {@link Optional} containing the {@link String} value, or empty if not found.
     */
    Optional<String> getString(Long chatId, String field);

    /**
     * Retrieves a UUID value from the session.
     *
     * @param chatId the chat session identifier
     * @param field the session key
     * @return the {@link Optional} containing the {@link UUID}, or empty if not found.
     */
    Optional<UUID> getUUID(Long chatId, String field);

    /**
     * Retrieves a LocalDate value from the session.
     *
     * @param chatId the chat session identifier
     * @param field the session key
     * @return the {@link Optional} containing the {@link LocalDate}, or empty if not found
     */
    Optional<LocalDate> getLocalDate(Long chatId, String field);

    /**
     * Retrieves an Integer value from the session.
     *
     * @param chatId the chat session identifier
     * @param field the session key
     * @return the {@link Optional} containing the {@link Integer}, or empty if not found
     */
    Optional<Integer> getInteger(Long chatId, String field);

    /**
     * Retrieves a BigDecimal value from the session.
     *
     * @param chatId the chat session identifier
     * @param field the session key
     * @return the {@link Optional} containing the {@link BigDecimal}, or empty if not found
     */
    Optional<BigDecimal> getBigDecimal(Long chatId, String field);

    /**
     * Retrieves a CarCategory value from the session.
     *
     * @param chatId the chat session identifier
     * @param field the session key
     * @return the {@link Optional} containing the {@link CarCategory}, or empty if not found.
     */
    Optional<CarCategory> getCarCategory(Long chatId, String field);

    /**
     * Retrieves the FlowContext value from the session.
     *
     * @param chatId the chat session identifier
     * @param field the session key
     * @return the {@link Optional} containing the {@link FlowContext}, or empty if not found
     */
    Optional<FlowContext> getFlowContext(Long chatId, String field);

    /**
     * Retrieves the CarBrowsingMode value from the session.
     *
     * @param chatId the chat session identifier
     * @param field the session key
     * @return the {@link Optional} containing the {@link CarBrowsingMode}, or empty if not found.
     */
    Optional<CarBrowsingMode> getCarBrowsingMode(Long chatId, String field);

    /**
     * Clears all session data for the given chat.
     *
     * @param chatId the chat session identifier
     */
    void deleteAll(Long chatId);
}

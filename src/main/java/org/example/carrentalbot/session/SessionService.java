package org.example.carrentalbot.session;

import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.model.enums.FlowContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;


/**
 * Service interface for managing user session state across distributed instances.
 * <p>Provides a type-safe API for persisting and retrieving temporary data
 * associated with a specific chat session, enabling multistep conversational
 * flows in a stateless environment.</p>
 */
public interface SessionService {

    /**
     * Stores a value in the session, associated with a specific field.
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "bookingId").
     * @param value  The object to persist; must be a supported type for serialization.
     */
    void put(Long chatId, String field, Object value);

    /**
     * Retrieves a String value from the session.
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "phone").
     * @return An {@link Optional} containing the value, or empty if not found.
     */
    Optional<String> getString(Long chatId, String field);

    /**
     * Retrieves and deserializes a UUID from the session.
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "carId").
     * @return An {@link Optional} containing the UUID, or empty if not found.
     */
    Optional<UUID> getUUID(Long chatId, String field);

    /**
     * Retrieves and deserializes a LocalDate from the session.
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "sartDate").
     * @return An {@link Optional} containing the date, or empty if not found.
     */
    Optional<LocalDate> getLocalDate(Long chatId, String field);

    /**
     * Retrieves and deserializes an Integer from the session.
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "totalDays").
     * @return An {@link Optional} containing the Integer, or empty if not found.
     */
    Optional<Integer> getInteger(Long chatId, String field);

    /**
     * Retrieves and deserializes a BigDecimal from the session.
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "totalCost").
     * @return An {@link Optional} containing the BigDecimal, or empty if not found.
     */
    Optional<BigDecimal> getBigDecimal(Long chatId, String field);

    /**
     * Retrieves a {@link CarCategory} enum constant from the session.
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "carCategory").
     * @return An {@link Optional} containing the {@link CarCategory}, or empty if not found.
     */
    Optional<CarCategory> getCarCategory(Long chatId, String field);

    /**
     * Retrieves a {@link FlowContext} enum constant to determine the current user state.
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "flowContext").
     * @return An {@link Optional} containing the {@link FlowContext}, or empty if not found.
     */
    Optional<FlowContext> getFlowContext(Long chatId, String field);

    /**
     * Retrieves a {@link CarBrowsingMode} enum constant from the session.
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "carBrowsingMode").
     * @return An {@link Optional} containing the {@link CarBrowsingMode}, or empty if not found.
     */
    Optional<CarBrowsingMode> getCarBrowsingMode(Long chatId, String field);

    /**
     * Purges all session data associated with the given chat ID.
     * <p>Typically invoked when a flow is completed, canceled, or reset.</p>
     * @param chatId The chat session to clear.
     */
    void deleteAll(Long chatId);
}

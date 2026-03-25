package org.example.carrentalbot.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis-backed implementation of {@link SessionService} using Hash structures.
 * <p>Data is stored as a Redis Hash where the key is prefixed with {@code chat:}
 * followed by the chatId. Each session is subject to a sliding expiration
 * defined by {@code DEFAULT_TTL}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    /** Prefix used for all Redis keys to ensure namespace isolation. */
    private static final String SESSION_PREFIX = "chat:";

    /** Maximum idle time for a session before it is automatically evicted by Redis. */
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    /**
     * Core Spring Data Redis helper used for performing high-level data operations.
     * <p>Configured to manage session state within Redis Hashes. It utilizes
     * {@link String} keys for chat-based identification and {@link Object} values
     * that undergo manual serialization to maintain type safety across
     * different conversational steps.</p>
     */
    private final RedisTemplate<String, Object> redisTemplate;

    private String key(Long chatId) {
        return SESSION_PREFIX + chatId;
    }

    /**
     * Serializes the value to a String and persists it in a Redis Hash.
     * <p>This method automatically refreshes the TTL of the entire session
     * hash upon every write operation.</p>
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "carCategory").
     * @param value  The object to persist; must be a supported type for serialization.
     * @throws IllegalArgumentException if chatId or field is null, or if the type is unsupported.
     */
    @Override
    public void put(Long chatId, String field, Object value) {
        if (chatId == null) {
            throw new IllegalArgumentException("chatId cannot be null");
        }
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        if (value == null) {
            return;
        }
        String serialized = (value instanceof String) ? (String) value : serializeValue(value);
        String redisKey = key(chatId);

        redisTemplate.opsForHash().put(redisKey, field, serialized);
        redisTemplate.expire(redisKey, DEFAULT_TTL);
    }

    /**
     * Internal helper to convert complex types into persistent String formats.
     * <p>Supported types: Enums, UUIDs, LocalDates, Integers, and BigDecimals.</p>
     * @param value The object to convert; must be a supported type for serialization.
     * @throws IllegalArgumentException if the type is unsupported.
     */
    private String serializeValue(Object value) {

        if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        }

        if (value instanceof UUID) {
            return value.toString();
        }

        if (value instanceof LocalDate) {
            return value.toString();
        }

        if (value instanceof Integer) {
            return value.toString();
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }

        throw new IllegalArgumentException(
                "Unsupported value type: " + value.getClass().getName()
        );
    }

    /**
     * Base retrieval method for raw String data from the Redis Hash.
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "phone").
     * @return An {@link Optional} containing the value, or empty if not found.
     */
    @Override
    public Optional<String> getString(Long chatId, String field) {
        return Optional.ofNullable((String) redisTemplate.opsForHash().get(key(chatId), field));
    }

    /**
     * Retrieves a UUID with fail-safe error handling.
     * <p>If the stored data is corrupted or in an invalid format,
     * the specific field is purged to maintain session integrity.</p>
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "carId").
     * @return An {@link Optional} containing the value, or empty if not found.
     * @throws IllegalArgumentException if the serialization fails.
     */
    @Override
    public Optional<UUID> getUUID(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(UUID.fromString(val));
                    } catch (IllegalArgumentException ex) {
                        log.error("Invalid UUID format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    /**
     * Retrieves a LocalDate with fail-safe error handling.
     * <p>If the stored data is corrupted or in an invalid format,
     * the specific field is purged to maintain session integrity.</p>
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "sartDate").
     * @return An {@link Optional} containing the value, or empty if not found.
     * @throws IllegalArgumentException if the serialization fails.
     */
    @Override
    public Optional<LocalDate> getLocalDate(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(LocalDate.parse(val));
                    } catch (DateTimeParseException ex) {
                        log.error("Invalid date format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }
    /**
     * Retrieves an Integer with fail-safe error handling.
     * <p>If the stored data is corrupted or in an invalid format,
     * the specific field is purged to maintain session integrity.</p>
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "totalDays").
     * @return An {@link Optional} containing the value, or empty if not found.
     * @throws IllegalArgumentException if the serialization fails.
     */
    @Override
    public Optional<Integer> getInteger(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(Integer.valueOf(val));
                    } catch (NumberFormatException ex) {
                        log.error("Invalid big decimal format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    /**
     * Retrieves an BigDecimal with fail-safe error handling.
     * <p>If the stored data is corrupted or in an invalid format,
     * the specific field is purged to maintain session integrity.</p>
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "totalCost").
     * @return An {@link Optional} containing the value, or empty if not found.
     * @throws IllegalArgumentException if the serialization fails.
     */
    @Override
    public Optional<BigDecimal> getBigDecimal(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(new BigDecimal(val));
                    } catch (NumberFormatException ex) {
                        log.error("Invalid big decimal format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    /**
     * Retrieves a {@link CarCategory} with fail-safe error handling.
     * <p>If the stored data is corrupted or in an invalid format,
     * the specific field is purged to maintain session integrity.</p>
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "carCategory").
     * @return An {@link Optional} containing the value, or empty if not found.
     * @throws IllegalArgumentException if the serialization fails.
     */
    @Override
    public Optional<CarCategory> getCarCategory(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(CarCategory.valueOf(val));
                    } catch (IllegalArgumentException ex) {
                        log.error("Invalid CarCategory enum format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    /**
     * Retrieves a {@link FlowContext} with fail-safe error handling.
     * <p>If the stored data is corrupted or in an invalid format,
     * the specific field is purged to maintain session integrity.</p>
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "flowContext").
     * @return An {@link Optional} containing the value, or empty if not found.
     * @throws IllegalArgumentException if the serialization fails.
     */
    @Override
    public Optional<FlowContext> getFlowContext(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(FlowContext.valueOf(val));
                    } catch (IllegalArgumentException ex) {
                        log.error("Invalid FlowContext enum format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    /**
     * Retrieves a {@link CarBrowsingMode} with fail-safe error handling.
     * <p>If the stored data is corrupted or in an invalid format,
     * the specific field is purged to maintain session integrity.</p>
     * @param chatId The unique identifier for the chat session.
     * @param field  The key representing the specific data point (e.g., "carBrowsingMode").
     * @return An {@link Optional} containing the value, or empty if not found.
     * @throws IllegalArgumentException if the serialization fails.
     */
    @Override
    public Optional<CarBrowsingMode> getCarBrowsingMode(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(CarBrowsingMode.valueOf(val));
                    } catch (IllegalArgumentException ex) {
                        log.error("Invalid CarBrowsingMode enum format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    /**
     * Deletes the entire Redis key associated with the chat.
     * @param chatId The chat session to clear.
     */
    @Override
    public void deleteAll(Long chatId) {
        redisTemplate.delete(key(chatId));
    }

    /**
     * Removes a specific field from the session hash.
     * @param chatId The chat session to clear.
     * @param field  The key representing the specific data point (e.g., "email").
     */
    private void delete(Long chatId, String field) {
        redisTemplate.opsForHash().delete(key(chatId), field);
    }
}

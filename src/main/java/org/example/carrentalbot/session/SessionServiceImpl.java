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
 * Session data is stored in Redis hashes under keys prefixed with {@code chat:}.
 * Each session has a sliding expiration controlled by a fixed TTL.
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
     * Spring Data Redis template used for session persistence.
     * Stores session data in Redis hashes keyed by chat identifier,
     * with values serialized as strings.
     */
    private final RedisTemplate<String, Object> redisTemplate;

    private String key(Long chatId) {
        return SESSION_PREFIX + chatId;
    }

    /**
     * {@inheritDoc}
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
     * Converts supported types into a String representation for Redis storage.
     * Supported types include enums, UUID, LocalDate, Integer, and BigDecimal.
     *
     * @throws IllegalArgumentException if the type is not supported
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
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getString(Long chatId, String field) {
        return Optional.ofNullable((String) redisTemplate.opsForHash().get(key(chatId), field));
    }

    /**
     * Retrieves and parses a UUID value from the session.
     * <p>If the stored value is invalid, the field is removed from the session.</p>
     *
     * @param chatId chat session identifier
     * @param field session key
     * @return the {@link Optional} containing the {@link UUID}, or empty if not found
     * @throws IllegalArgumentException if the serialization fails
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
     * Retrieves and parses a LocalDate value from the session.
     * <p>If the stored value is invalid, the field is removed from the session.</p>
     *
     * @param chatId chat session identifier
     * @param field session key
     * @return the {@link Optional} containing the {@link LocalDate}, or empty if not found
     * @throws IllegalArgumentException if the serialization fails
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
     * Retrieves and parses an Integer value from the session.
     * <p>If the stored value is invalid, the field is removed from the session.</p>
     *
     * @param chatId chat session identifier
     * @param field session key
     * @return the {@link Optional} containing the {@link Integer}, or empty if not found
     * @throws IllegalArgumentException if the serialization fails
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
     * Retrieves and parses a BigDecimal value from the session.
     * <p>If the stored value is invalid, the field is removed from the session.</p>
     *
     * @param chatId chat session identifier
     * @param field session key
     * @return the {@link Optional} containing the {@link BigDecimal}, or empty if not found
     * @throws IllegalArgumentException if the serialization fails
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
     * Retrieves and parses a CarCategory value from the session.
     * <p>If the stored value is invalid, the field is removed from the session.</p>
     *
     * @param chatId chat session identifier
     * @param field session key
     * @return the {@link Optional} containing the {@link CarCategory}, or empty if not found
     * @throws IllegalArgumentException if the serialization fails
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
     * Retrieves and parses a FlowContext value from the session.
     * <p>If the stored value is invalid, the field is removed from the session.</p>
     *
     * @param chatId chat session identifier
     * @param field session key
     * @return the {@link Optional} containing the {@link FlowContext}, or empty if not found
     * @throws IllegalArgumentException if the serialization fails
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
     * Retrieves and parses a CarBrowsingMode value from the session.
     * <p>If the stored value is invalid, the field is removed from the session.</p>
     *
     * @param chatId chat session identifier
     * @param field session key
     * @return the {@link Optional} containing the {@link CarBrowsingMode}, or empty if not found
     * @throws IllegalArgumentException if the serialization fails
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
     * {@inheritDoc}
     */
    @Override
    public void deleteAll(Long chatId) {
        redisTemplate.delete(key(chatId));
    }

    /**
     * Removes a specific field from the session hash.
     *
     * @param chatId chat session identifier
     * @param field  session key
     */
    private void delete(Long chatId, String field) {
        redisTemplate.opsForHash().delete(key(chatId), field);
    }
}

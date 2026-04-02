package org.example.carrentalbot.aop;

import lombok.Getter;

/**
 * Defines keys used for MDC (Mapped Diagnostic Context) logging.
 * <p>These fields are used to enrich log entries with contextual information
 * such as Telegram update metadata, user identity, and execution tracing data.</p>
 */
@Getter
public enum MDCFields {

    /** Unique trace identifier for tracking a request across services. */
    TRACE_ID ("trace_id"),


    /** Telegram update identifier. */
    UPDATE_ID ("update_id"),

    /** Type of Telegram update (e.g., message, callback_query). */
    UPDATE_TYPE ("update_type"),

    /** Chat identifier where the update originated. */
    CHAT_ID ("chat_id"),

    /** Telegram user identifier. */
    TELEGRAM_USER_ID ("telegram_user_id"),


    /** Class name where the log entry was created. */
    CLASS ("class"),

    /** Method name where the log entry was created. */
    METHOD ("method");

    private final String value;

    /**
     * Creates an MDC field with the specified key name.
     *
     * @param value the string key used in MDC logging context
     */
    MDCFields (String value) {
        this.value = value;
    }
}
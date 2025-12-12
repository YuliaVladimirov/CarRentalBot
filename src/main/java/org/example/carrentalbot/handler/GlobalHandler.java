package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.UpdateDto;

/**
 * Primary entry point for processing incoming Telegram updates.
 *
 * <p>Implementations of this interface are responsible for inspecting the
 * {@link UpdateDto} payload and delegating it to the appropriate handler
 * (e.g., message, command, text, or callback query processing).</p>
 *
 * <p>This abstraction intentionally exposes only a single method to keep
 * the dispatching logic encapsulated and allow internal routing to evolve
 * without affecting callers such as the webhook controller.</p>
 */

public interface GlobalHandler {

    /**
     * Handles an incoming Telegram update.
     *
     * @param update the update payload received from Telegram; may be {@code null}
     */
    void handleUpdate(UpdateDto update);
}

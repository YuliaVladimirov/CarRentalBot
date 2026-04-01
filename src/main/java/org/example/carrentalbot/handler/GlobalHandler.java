package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.UpdateDto;

/**
 * Core contract for processing incoming updates from the Telegram Bot API.
 * <p>Implementations of this interface act as the central dispatcher responsible for
 * analyzing the content of an {@link UpdateDto} and routing it to the appropriate
 * handler based on its type (e.g. message, command, callback query).</p>
 */
public interface GlobalHandler {

    /**
     * Processes a single incoming update from the Telegram Bot API.
     * <p>The implementation is responsible for inspecting the {@code update}
     * (e.g. message, callback query) and delegating processing to the appropriate
     * component.</p>
     *
     * @param update non-null Telegram update containing all incoming data
     */
    void handleUpdate(UpdateDto update);
}

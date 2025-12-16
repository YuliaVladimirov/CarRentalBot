package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.UpdateDto;

/**
 * Defines the core contract for processing all incoming updates from the
 * Telegram Bot API.
 * <p>Implementations of this interface act as the central dispatcher, analyzing the
 * type of content within the {@link UpdateDto} (e.g., message, command, callback query)
 * and routing it to the appropriate specialized handler.</p>
 */
public interface GlobalHandler {

    /**
     * Handles a single, incoming update received from the Telegram Bot API.
     * <p>The implementation should inspect the contents of the {@code update} object
     * (e.g., check for {@code message()}, {@code callback_query()}, etc.) and
     * dispatch the processing to the correct component based on the update type.</p>
     * @param update The data transfer object containing the full update details
     * from Telegram. This object is never {@code null}.
     */
    void handleUpdate(UpdateDto update);
}

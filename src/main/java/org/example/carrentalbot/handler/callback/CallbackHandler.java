package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.model.enums.FlowContext;

import java.util.EnumSet;

/**
 * Defines the contract for processing user interactions originating from a
 * Telegram inline keyboard button press (a callback query).
 * <p>Each implementation of this interface is responsible for handling a specific
 * type of interaction, typically identified by a unique prefix in the callback data.</p>
 */
public interface CallbackHandler {

    /**
     * Retrieves the unique key or prefix string that this handler is responsible for.
     * <p>This key is matched against the beginning of the incoming callback data
     * to determine which handler should process the query.</p>
     * @return The unique string key for this handler (e.g., "BROWSE_ALL_CARS", "CONFIRM_BOOKING" or "CALENDAR_PICK"  ).
     */
    String getKey();

    /**
     * Returns a set of allowed application states (flow contexts) in which this
     * callback handler is permitted to execute its logic.
     * <p>This is used by the dispatcher (e.g., {@code GlobalHandler}) to ensure
     * the user is not attempting to perform an action outside a defined workflow.
     * If the implementation returns {@link EnumSet#allOf(Class)}, it signifies
     * that the handler can be executed regardless of the user's current flow state.</p>
     * @return An {@link EnumSet} of {@link FlowContext} constants that allow execution.
     */
    EnumSet<FlowContext> getAllowedContexts();

    /**
     * Executes the main business logic required to process the callback query.
     * <p>Implementations should typically update the user's flow state, send
     * a response message, or modify the message that contains the inline keyboard.</p>
     * @param chatId The ID of the Telegram chat where the callback originated.
     * @param callbackQuery The data transfer object containing the full callback
     * details, including the user and the callback data payload.
     */
    void handle(Long chatId, CallbackQueryDto callbackQuery);
}

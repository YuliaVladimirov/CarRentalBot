package org.example.carrentalbot.handler.command;

import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.model.enums.FlowContext;

import java.util.EnumSet;

/**
 * Defines the contract for processing slash-commands and universal bot instructions.
 * <p>Each implementation  of this interface is responsible for defining a unique command string (e.g., "/start")
 * and handling the business logic associated with that direct user input.</p>
 */
public interface CommandHandler {

    /**
     * Returns the specific command string this handler is responsible for.
     * @return The command string (e.g., "/start", "/help" or an internal fallback key).
     */
    String getCommand();

    /**
     * Returns a set of allowed application states (flow contexts) in which this
     * command handler is permitted to execute its logic.
     * <p>This is used by the dispatcher (e.g., {@code GlobalHandler}) to ensure
     * the user is not attempting to perform an action outside a defined workflow.</p>
     * If the implementation returns {@link EnumSet#allOf(Class)}, it signifies
     * that the handler can be executed regardless of the user's current flow state.</p>
     * @return An {@link EnumSet} of {@link FlowContext} constants that allow execution.
     */
    EnumSet<FlowContext> getAllowedContexts();

    /**
     * Executes the main business logic for the provided command input.
     * <p>Implementations should typically update the user's flow state,
     * managing session store, and send a response message with the appropriate inline keyboard
     * to advance the user to the next step in the flow.
     * @param chatId The ID of the chat where the command was issued.
     * @param from Metadata about the Telegram user who sent the command.
     */
    void handle(Long chatId, FromDto from);
}

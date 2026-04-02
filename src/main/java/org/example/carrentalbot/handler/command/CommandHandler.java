package org.example.carrentalbot.handler.command;

import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.model.enums.FlowContext;

import java.util.EnumSet;

/**
 * Contract for handling slash commands and global bot actions.
 * <p>Each implementation processes a specific command (e.g. "/start", "/help")
 * and defines the logic executed for that user input.</p>
 */
public interface CommandHandler {

    /**
     * Returns the command handled by this implementation.
     *
     * @return command string (e.g. "/start", "/help")
     */
    String getCommand();

    /**
     * Returns the flow contexts in which this handler is allowed to execute.
     * <p>Flow context prevents users from performing actions outside the intended workflow.</p>
     *
     * @return allowed {@link FlowContext} values for execution
     */
    EnumSet<FlowContext> getAllowedContexts();

    /**
     * Processes the command.
     * <p>Implementations typically update session state and send a response
     * to guide the user through the next step.</p>
     *
     * @param chatId chat identifier where the command was issued
     * @param from user metadata from Telegram
     */
    void handle(Long chatId, FromDto from);
}

package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.model.enums.FlowContext;

import java.util.EnumSet;

/**
 * Defines the contract for processing raw text messages sent by the user.
 * <p>Each implementation of this interface is responsible for identifying and
 * handling specific text patterns, commands, or state-dependent user inputs
 * (such as providing phone numbers, emails, or search queries).</p>
 */
public interface TextHandler {

    /**
     * Determines whether this handler is capable of processing the provided text.
     * <p>This method checks the text against a regular expression or verifying its length and format.</p>
     * @param text The raw text message received from the user.
     * @return {@code true} if this handler can process the message; {@code false} otherwise.
     */
    boolean canHandle(String text);

    /**
     * Returns a set of allowed application states (flow contexts) in which this
     * text handler is permitted to execute its logic.
     * <p>This is used by the dispatcher (e.g., {@code GlobalHandler}) to ensure
     * that a user's text input (e.g., a phone number) is only processed when the bot is actually
     * expecting that specific piece of information.</p>
     * If the implementation returns {@link EnumSet#allOf(Class)}, it signifies
     * that the handler can be executed regardless of the user's current flow state.</p>
     * @return An {@link EnumSet} of {@link FlowContext} constants that allow execution.
     */
    EnumSet<FlowContext> getAllowedContexts();

    /**
     * Executes the main business logic for the provided text input.
     * <p>Implementations should typically update the user's flow state, validate the data,
     * persist it to the session store, and advance the user to the next step in the flow.</p>
     * @param chatId The ID of the Telegram chat where the message originated.
     * @param text The validated text input from the user.
     */
    void handle(Long chatId, String text);
}

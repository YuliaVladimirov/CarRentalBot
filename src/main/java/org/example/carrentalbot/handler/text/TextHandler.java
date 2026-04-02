package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.model.enums.FlowContext;

import java.util.EnumSet;

/**
 * Contract for handling free-form text input from users.
 * <p>Each implementation processes specific text patterns (e.g. phone numbers,
 * emails, search queries) or state-dependent user input within a flow.</p>
 */
public interface TextHandler {

    /**
     * Checks whether this handler can process the given text.
     * <p>Evaluates the input string against the regex pattern.</p>
     *
     * @param text raw user input
     * @return {@code true} if the text can be handled, otherwise {@code false}
     */
    boolean canHandle(String text);

    /**
     * Returns the flow contexts in which this handler is allowed to execute.
     * <p>Flow context prevents users from performing actions outside the intended workflow.</p>
     *
     * @return allowed {@link FlowContext} values for execution
     */
    EnumSet<FlowContext> getAllowedContexts();

    /**
     * Processes the user input.
     * <p>Implementations typically validate input, update session state,
     * persist data, and move the user to the next step in the flow.</p>
     *
     * @param chatId chat identifier where the message originated
     * @param text user input text
     */
    void handle(Long chatId, String text);
}

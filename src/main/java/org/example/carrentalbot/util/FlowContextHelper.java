package org.example.carrentalbot.util;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.exception.InvalidFlowContextException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Helper component for validating whether a handler is allowed to run
 * in the user's current conversational flow context.
 * <p>This class ensures that a user's current session state (FlowContext) aligns with
 * the permitted contexts for a specific action or command.</p>
 * <p>If a handler is invoked in an invalid context, an
 * {@link InvalidFlowContextException} is thrown with a user-friendly message.</p>
 */
@Component
@RequiredArgsConstructor
public class FlowContextHelper {

    /**
     * Service used to retrieve and manage session-specific data.
     */
    private final SessionService sessionService;

    /**
     * Validates if the current flow context for a given chat ID is permitted.
     * <p>The validation logic follows these rules:
     * <ul>
     *     <li>If a context exists, it must be present in the {@code allowedContexts} set.</li>
     *     <li>If no context exists, the action is only permitted if {@code allowedContexts}
     *     contains all possible {@link FlowContext} values (representing a global/open state).</li>
     * </ul>
     * </p>
     * @param chatId          The unique identifier of the chat/user session.
     * @param allowedContexts An {@link EnumSet} of {@link FlowContext} values that are valid for this operation.
     * @throws InvalidFlowContextException if the current context is not allowed or is missing when required.
     */
    public void validateFlowContext(Long chatId, EnumSet<FlowContext> allowedContexts) {
        FlowContext current = sessionService
                .getFlowContext(chatId, "flowContext")
                .orElse(null);

        if (current != null && !allowedContexts.contains(current)) {
            throw new InvalidFlowContextException(current.getErrorMessage());
        }

        if (current == null && !allowedContexts.equals(EnumSet.allOf(FlowContext.class))) {
            throw new InvalidFlowContextException("This option is not available right now.");
        }
    }
}

package org.example.carrentalbot.util;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.exception.InvalidFlowContextException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Helper component responsible for validating whether a handler is allowed to execute
 * within the user's current conversational {@link FlowContext}.
 * <p>Ensures that the current session state is compatible with the allowed contexts
 * defined for a given operation. This prevents invalid transitions between
 * conversational states.</p>
 * <p>If a handler is invoked in an invalid context, an
 * {@link InvalidFlowContextException} is thrown with a user-friendly message.</p>
 */
@Component
@RequiredArgsConstructor
public class FlowContextHelper {

    private final SessionService sessionService;

    /**
     * Validates whether the current flow context for the specified chat is allowed
     * for the requested operation.
     * <p>Validation rules:
     * <ul>
     *   <li>If a current context exists, it must be included in {@code allowedContexts}</li>
     *   <li>If no context exists, the operation is allowed only when {@code allowedContexts}
     *       contains all {@link FlowContext} values (global/unrestricted state)</li>
     * </ul>
     * @param chatId unique identifier of the chat/session
     * @param allowedContexts set of permitted {@link FlowContext} values for this operation
     * @throws InvalidFlowContextException if the current context is not permitted or missing
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

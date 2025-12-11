package org.example.carrentalbot.util;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.exception.InvalidFlowContextException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Utility component responsible for validating whether a handler is allowed to run
 * in the user's current conversational flow context.
 *
 * <p>The application associates each chat session with a {@link FlowContext}
 * (e.g., browsing, booking, editing). Handlers declare the flow contexts in which
 * they are permitted to operate, and this helper ensures that the invocation is
 * valid.</p>
 *
 * <p>If a handler is invoked in an invalid context, an
 * {@link InvalidFlowContextException} is thrown with a user-friendly message.</p>
 */
@Component
@RequiredArgsConstructor
public class FlowContextHelper {

    private final SessionService sessionService;

    /**
     * Validates that the user's current flow context is allowed for the handler.
     *
     * <p>This method retrieves the active {@link FlowContext} from the session store.
     * If the current context is not among the allowed ones, or if no context exists
     * and the handler is not universally allowed, an exception is thrown.</p>
     *
     * <p>A handler is considered universally allowed when its
     * {@code allowedContexts} equals {@code EnumSet.allOf(FlowContext.class)}.</p>
     *
     * @param chatId the ID of the Telegram chat whose context is being validated
     * @param allowedContexts the contexts in which the handler may execute
     *
     * @throws InvalidFlowContextException if execution is not allowed for the
     *                                     current flow context
     */
    public void validateFlowContext(Long chatId, EnumSet<FlowContext> allowedContexts) {
        FlowContext current = sessionService
                .getFlowContext(chatId, "flowContext")
                .orElse(null);

        if (current != null && !allowedContexts.contains(current)) {
            throw new InvalidFlowContextException(getInvalidContextMessage(current));
        }

        if (current == null && !allowedContexts.equals(EnumSet.allOf(FlowContext.class))) {
            throw new InvalidFlowContextException(getInvalidContextMessage(null));
        }
    }

    /**
     * Produces a user-friendly validation failure message based on the current flow.
     *
     * @param current the user's active flow context; may be {@code null}
     * @return a descriptive error message indicating why the handler is unavailable
     */
    private String getInvalidContextMessage(FlowContext current) {
        if (current == null) {
            return "This action is not available right now.";
        }

        return switch (current) {
            case BROWSING_FLOW ->
                    "The handler is not available in browsing flow.";
            case BOOKING_FLOW ->
                    "The handler is not available in booking flow.";
            case EDIT_BOOKING_FLOW ->
                    "The handler is not available in editing booking flow.";
            case MY_BOOKINGS_FLOW ->
                    "The handler is not available in my booking flow.";
        };
    }
}

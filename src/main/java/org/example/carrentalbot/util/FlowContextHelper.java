package org.example.carrentalbot.util;

import org.example.carrentalbot.exception.InvalidFlowContextException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.SessionService;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class FlowContextHelper {

    private final SessionService sessionService;

    public FlowContextHelper(SessionService sessionService) {
        this.sessionService = sessionService;
    }

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

package org.example.carrentalbot.util;

import org.example.carrentalbot.exception.DataNotFoundException;
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
        FlowContext current = sessionService.get(chatId, "flowContext", FlowContext.class)
                .orElse(null);

        if (current != null && !allowedContexts.contains(current)) {
            throw new InvalidFlowContextException(chatId,
                    "This action is not allowed in the current state. Please start from the main menu.");
        }

        if (current == null && !allowedContexts.equals(EnumSet.allOf(FlowContext.class))) {
            throw new InvalidFlowContextException(chatId,
                    "This action is not allowed in the current state. Please start from the main menu.");
        }

    }

    public FlowContext getFlowContext(Long chatId) {

        return sessionService.get(chatId, "flowContext", FlowContext.class)
                .orElseThrow(() -> new DataNotFoundException(chatId, "Flow context not found."));
    }

    public void setFlowContext(Long chatId, FlowContext context) {
        sessionService.put(chatId, "flowContext", context);
    }
}

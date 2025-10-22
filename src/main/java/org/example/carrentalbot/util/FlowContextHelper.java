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
            throw new InvalidFlowContextException(chatId, getInvalidContextMessage(current));
        }

        if (current == null && !allowedContexts.equals(EnumSet.allOf(FlowContext.class))) {
            throw new InvalidFlowContextException(chatId, getInvalidContextMessage(null));
        }
    }

    public FlowContext getFlowContext(Long chatId) {

        return sessionService.get(chatId, "flowContext", FlowContext.class)
                .orElseThrow(() -> new DataNotFoundException(chatId, "Flow context not found."));
    }

    public void setFlowContext(Long chatId, FlowContext context) {
        sessionService.put(chatId, "flowContext", context);
    }

    private String getInvalidContextMessage(FlowContext current) {
        if (current == null) {
            return "This action is not available right now. Please return to the main menu.";
        }

        return switch (current) {
            case BROWSING_FLOW ->
                    "This action is not available in browsing flow. Please continue your current flow or return to the main menu.";
            case BOOKING_FLOW ->
                    "This action is not available in booking flow. Please finish booking, cancel it or return to the main menu.";
            case EDIT_BOOKING_FLOW ->
                    "This action is not available in editing flow. Please finish editing, confirm or cancel booking or return to the main menu.";
            case MY_BOOKINGS_FLOW ->
                    "This action is not available in current flow. Please finish viewing or editing your bookings, or return to the main menu.";
        };
    }
}

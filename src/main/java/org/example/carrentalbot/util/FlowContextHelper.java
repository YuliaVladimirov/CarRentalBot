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
            return "This action is not available right now. Please continue your current flow or return to the main menu.";
        }

        return switch (current) {
            case BROWSING_FLOW ->
                    "You are currently browsing cars. This action is not available in browsing. Please continue your current flow or return to the main menu.";
            case BOOKING_FLOW ->
                    "⚠️ You are in the middle of a booking. This action is not available in booking. Please finish booking or cancel it or return to the main menu.";
            case EDIT_BOOKING_FLOW ->
                    "⚠️ You are editing a booking. This action is not available in editing. Please finish editing or cancel booking or return to the main menu.";
            case MY_BOOKINGS_FLOW ->
                    "⚠️ You are currently managing your bookings. This action is not available right now. Please finish viewing or editing your bookings, or return to the main menu.";
        };
    }
}

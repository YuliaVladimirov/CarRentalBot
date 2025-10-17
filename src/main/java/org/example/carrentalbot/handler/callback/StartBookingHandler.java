package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class StartBookingHandler implements CallbackHandler {

    public static final String KEY = "START_BOOKING";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final AskForPhoneHandler askForPhoneHandler;
    private final SessionService sessionService;
    private final NavigationService navigationService;

    public StartBookingHandler(AskForPhoneHandler askForPhoneHandler, SessionService sessionService, NavigationService navigationService) {
        this.askForPhoneHandler = askForPhoneHandler;
        this.sessionService = sessionService;
        this.navigationService = navigationService;
    }

    @Override
    public String getKey() { return KEY; }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        navigationService.push(chatId, KEY);

        sessionService.put(chatId, "flowContext", FlowContext.BOOKING_FLOW);

        askForPhoneHandler.handle(chatId, callbackQuery);
    }
}

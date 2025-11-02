package org.example.carrentalbot.handler.command;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.handler.callback.DisplayMyBookingsHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class MyBookingsCommandHandler implements CommandHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final DisplayMyBookingsHandler displayMyBookingsHandler;

    public MyBookingsCommandHandler(DisplayMyBookingsHandler displayMyBookingsHandler) {
        this.displayMyBookingsHandler = displayMyBookingsHandler;
    }

    @Override
    public String getCommand() {
        return "/bookings";
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, MessageDto message) {

        CallbackQueryDto callback = CallbackQueryDto.builder()
                .from(FromDto.builder()
                        .id(message.getFrom().getId())
                        .build())
                .data(DisplayMyBookingsHandler.KEY)
                .build();

        displayMyBookingsHandler.handle(chatId, callback);
    }
}

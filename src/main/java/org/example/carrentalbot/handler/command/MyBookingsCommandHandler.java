package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.handler.callback.DisplayMyBookingsHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyBookingsCommandHandler implements CommandHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final DisplayMyBookingsHandler displayMyBookingsHandler;

    @Override
    public String getCommand() {
        return "/bookings";
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, FromDto from) {
        log.info("Processing '/bookings' flow");

        CallbackQueryDto callback = CallbackQueryDto.builder()
                .from(from)
                .data(DisplayMyBookingsHandler.KEY)
                .build();

        displayMyBookingsHandler.handle(chatId, callback);
    }
}

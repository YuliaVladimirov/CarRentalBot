package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.handler.callback.MainMenuHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@RequiredArgsConstructor
public class MainCommandHandler implements CommandHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final MainMenuHandler mainMenuHandler;

    @Override
    public String getCommand() {
        return "/main";
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, FromDto from) {
        mainMenuHandler.handle(chatId, null);
    }
}

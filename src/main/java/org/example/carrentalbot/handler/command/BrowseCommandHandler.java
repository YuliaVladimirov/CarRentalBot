package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.handler.callback.BrowseCategoriesHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@RequiredArgsConstructor
public class BrowseCommandHandler implements CommandHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final BrowseCategoriesHandler browseCategoriesHandler;

    @Override
    public String getCommand() {
        return "/browse";
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, FromDto from) {
        browseCategoriesHandler.handle(chatId, null);
    }
}

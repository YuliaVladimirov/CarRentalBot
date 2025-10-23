package org.example.carrentalbot.handler.command;

import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.handler.callback.BrowseCategoriesHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class BrowseCommandHandler implements CommandHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final BrowseCategoriesHandler browseCategoriesHandler;

    public BrowseCommandHandler(BrowseCategoriesHandler browseCategoriesHandler) {
        this.browseCategoriesHandler = browseCategoriesHandler;
    }

    @Override
    public String getCommand() {
        return "/browse";
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, MessageDto message) {

        browseCategoriesHandler.handle(chatId, null);
    }
}

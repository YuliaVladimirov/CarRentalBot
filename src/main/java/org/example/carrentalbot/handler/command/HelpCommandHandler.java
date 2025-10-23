package org.example.carrentalbot.handler.command;

import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.handler.callback.HelpMenuHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class HelpCommandHandler implements CommandHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final HelpMenuHandler helpMenuHandler;

    public HelpCommandHandler(HelpMenuHandler helpMenuHandler) {
        this.helpMenuHandler = helpMenuHandler;
    }


    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, MessageDto message) {

        helpMenuHandler.handle(chatId,null);
    }
}

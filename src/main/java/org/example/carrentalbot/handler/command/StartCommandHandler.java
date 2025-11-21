package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.handler.callback.MainMenuHandler;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CustomerServiceImpl;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final CustomerServiceImpl customerService;
    private final MainMenuHandler mainMenuHandler;
    private final TelegramClient telegramClient;

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, FromDto from) {
        Customer customer = customerService.registerIfNotExists(chatId, from);

        String welcomeText = String.format("""
             Hi, %s!
             Welcome to Car Rental Bot.
             """, customer.getFirstName());

                telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(welcomeText)
                .parseMode("HTML")
                .build());

        mainMenuHandler.handle(chatId, null);
    }
}

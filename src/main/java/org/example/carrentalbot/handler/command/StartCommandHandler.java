package org.example.carrentalbot.handler.command;

import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.handler.callback.GoToMainMenuHandler;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CustomerService;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class StartCommandHandler implements CommandHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final CustomerService customerService;
    private final GoToMainMenuHandler goToMainMenuHandler;
    private final TelegramClient telegramClient;

    public StartCommandHandler(CustomerService customerService,
                               GoToMainMenuHandler goToMainMenuHandler,
                               TelegramClient telegramClient) {
        this.customerService = customerService;
        this.goToMainMenuHandler = goToMainMenuHandler;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, MessageDto message) {
        Customer customer = customerService.registerIfNotExists(chatId, message.getFrom());

        String welcomeText = String.format("""
             Hi, %s!
             Welcome to Car Rental Bot.
             """, customer.getFirstName());

                telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(welcomeText)
                .parseMode("HTML")
                .build());

        goToMainMenuHandler.handle(chatId, null);
    }
}

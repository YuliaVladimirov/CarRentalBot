package org.example.carrentalbot.handler.command;

import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.service.CustomerService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class StartCommandHandler implements CommandHandler {

    private final CustomerService customerService;
    private final NavigationService navigationService;
    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

    public StartCommandHandler(CustomerService customerService,
                               NavigationService navigationService,
                               TelegramClient telegramClient,
                               KeyboardFactory keyboardFactory) {
        this.customerService = customerService;
        this.navigationService = navigationService;
        this.telegramClient = telegramClient;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public String getCommand() {
        return "/start";
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

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildMainMenuKeyboard();

        navigationService.push(chatId, "GO_TO_MAIN_MENU");
        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("Main Menu")
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

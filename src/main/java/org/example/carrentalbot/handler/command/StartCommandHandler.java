package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.handler.callback.MainMenuHandler;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.record.CustomerRegistration;
import org.example.carrentalbot.service.CustomerServiceImpl;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Slf4j
@Service
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
        log.info("Processing '/start' flow");

        CustomerRegistration customerRegistration = customerService.registerIfNotExists(chatId, from);
        Customer customer = customerRegistration.customer();
        String welcomeText;

        if (customerRegistration.isNew()) {
            log.info("New customer created: customerId={}", customer.getId());

            welcomeText = String.format("""
                    Hi, %s!
                    Welcome to Car Rental Bot.
                    You've just joined the Car Rental Bot!
                    """, customer.getFirstName());
        } else {
            log.info("Returning customer: customerId={}", customer.getId());

            welcomeText = String.format("""
                    Hi, %s!
                    Welcome back to Car Rental Bot.
                    Glad to see you again.
                    """, customer.getFirstName());
        }
        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(welcomeText)
                .parseMode("HTML")
                .build());

        mainMenuHandler.handle(chatId, null);
    }
}

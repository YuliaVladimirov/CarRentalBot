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

/**
 * Concrete implementation of the {@link CommandHandler} interface.
 * <p>This service is the primary entry point for all users of the Car Rental Bot.
 * This handler is responsible for:
 * <ul>
 * <li>Providing a unique identifier {@code COMMAND} for proper command routing.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Idempotent registration of the customer in the database.</li>
 * <li>Differentiating between new users and returning customers for personalized greetings.</li>
 * <li>Immediate hand-off to the {@link MainMenuHandler} to present available services.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    /**
     * The unique routing identifier used to identify {@code StartCommandHandler} and properly route commands.
     */
    public static final String COMMAND = "/start";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Configured to {@link EnumSet#allOf(Class)} to ensure
     * that a reset or restart can be possible regardless of the user's current session state.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Service responsible for persisting or retrieving the {@link Customer} record based on
     * Telegram user information.
     */
    private final CustomerServiceImpl customerService;

    /**
     * Handler responsible for displaying the initial navigation options after the welcome message.
     */
    private final MainMenuHandler mainMenuHandler;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically for delivering the personalized welcome text.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     * @return The constant {@code COMMAND}.
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }

    /**
     * {@inheritDoc}
     * @return {@link #ALLOWED_CONTEXTS}.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Orchestrates the user's first interaction with the bot.
     * <ol>
     * <li>Invokes {@code registerIfNotExists} to ensure a database record exists for the user.</li>
     * <li>Checks {@code CustomerRegistration.isNew()} to branch the greeting logic.</li>
     * <li>Sends a "Hi!" or "Welcome back!" message via the {@link TelegramClient}.</li>
     * <li>Delegates control to the {@link MainMenuHandler} to present the main interface.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param from The Telegram user data used for registration and personalization.
     */
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

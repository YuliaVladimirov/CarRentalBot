package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.record.CarProjection;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

/**
 * Callback handler responsible for starting the car browsing flow.
 * <p>Displays available vehicle categories and transitions the user into the
 * browsing state of the application.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseCategoriesHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "BROWSE_CATEGORIES";

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler can be triggered from any application state to allow
     * users to start browsing cars at any time.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Service for retrieving car category data.
     */
    private final CarService carService;

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building car category selection keyboards.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Client for sending messages via the Telegram Bot API.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Starts the car browsing flow and displays available categories to the user.

     * @param chatId chat identifier
     * @param callbackQuery callback payload triggering the browsing flow
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'browse car categories' flow");

        sessionService.put(chatId, "flowContext", FlowContext.BROWSING_FLOW);
        log.debug("Session updated: 'flowContext' set to {}", FlowContext.BROWSING_FLOW);

        List<CarProjection> carCategories = carService.getCarCategories();
        log.info("Fetched {} car categories", carCategories.size());


        InlineKeyboardMarkupDto keyboard = keyboardFactory.buildCarCategoryKeyboard(carCategories);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("<b>Available Categories:</b>")
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build());
    }
}

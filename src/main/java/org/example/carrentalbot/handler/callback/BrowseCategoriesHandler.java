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
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service initiates the car discovery process by displaying available
 * vehicle categories. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code BrowseCategoriesHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Transitioning the user's session state to {@link FlowContext#BROWSING_FLOW}.</li>
 * <li>Retrieving the list of distinct car categories from the {@link CarService}.</li>
 * <li>Constructing the category-selection menu via {@link KeyboardFactory}.</li>
 *  <li>Dispatching the category-selection menu message with appropriate formatting and markup.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseCategoriesHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code BrowseCategoriesHandler} and properly route callbacks.
     */
    public static final String KEY = "BROWSE_CATEGORIES";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Uses {@link EnumSet#allOf(Class)} to allow users to start browsing
     * cars from any point in the application.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Service responsible for retrieving car category data and inventory information.
     */
    private final CarService carService;

    /**
     * Service responsible for managing user-specific session data, specifically the
     * active {@link FlowContext}.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline keyboard that displays car categories.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to display the category list.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     * @return The constant {@link #KEY}.
     */
    @Override
    public String getKey() {
        return KEY;
    }

    /**
     * {@inheritDoc}
     * @return A set containing all possible {@link FlowContext} values.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Processes the request to browse car categories and updates the user's flow state.
     * <ol>
     * <li>Logs the start of the browsing flow.</li>
     * <li>Updates the user's session in {@link SessionService}, setting the current
     * flow to {@link FlowContext#BROWSING_FLOW}.</li>
     * <li>Fetches the list of available {@link CarProjection} categories from the database.</li>
     * <li>Invokes {@link KeyboardFactory} to generate a keyboard based on the retrieved categories.</li>
     * <li>Sends the category selection message to the user via the Telegram API.</li>
     * </ol>
     * @param chatId The ID of the chat where the categories should be displayed.
     * @param callbackQuery The incoming callback query DTO.
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

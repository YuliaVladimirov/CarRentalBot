package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service filters and displays vehicle inventory based on a user-specified
 * date range. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code BrowseCarsForDatesHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Restricting execution to {@link FlowContext#BROWSING_FLOW}.</li>
 * <li>Validating the presence of required session data (category, start date, and end date).</li>
 * <li>Retrieving available vehicles from {@link CarService} that are not booked during the period.</li>
 * <li>Delivering a filtered vehicle selection interface to the user.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseCarsForDatesHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code BrowseCarsForDatesHandler} and properly route callbacks.
     */
    public static final String KEY = "BROWSE_CARS_FOR_DATES";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BROWSING_FLOW} to ensure the date-filtered car search
     * only occurs during the browsing lifecycle.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service responsible for checking vehicle availability in the inventory based on specific dates.
     */
    private final CarService carService;

    /**
     * Service responsible for managing user-specific session data, specifically the
     * chosen {@link CarCategory}, {@code startDate} and {@code endDate} (both in {@link LocalDate} format).
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline keyboard
     * that displays the car list available for selected rental period.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to display the car list in the selected category for the chosen dates.
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
     * @return {@link #ALLOWED_CONTEXTS}.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Processes the request to search for available cars within a specific date range.
     * <ol>
     * <li>Logs the start of the date-filtered browsing flow.</li>
     * <li>Retrieves the target {@link CarCategory} from the session.</li>
     * <li>Retrieves the {@code startDate} and {@code endDate} from the session.</li>
     * <li>Invokes {@link CarService} to find cars that are available for the entire duration.</li>
     * <li>Invokes {@link KeyboardFactory} to build a selection keyboard using the list of available vehicles.</li>
     * <li>Sends an HTML-formatted message to the user displaying the available options.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if the category or any of the dates are missing from the session.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'browse cars for dates' flow");

        CarCategory carCategory = sessionService
                .getCarCategory(chatId, "carCategory")
                .orElseThrow(() -> new DataNotFoundException("Category not found in session"));
        log.debug("Loaded from session: carCategory={}", carCategory);

        LocalDate startDate = sessionService
                .getLocalDate(chatId, "startDate")
                .orElseThrow(() -> new DataNotFoundException("Start date not found in session"));
        log.debug("Loaded from session: startDate={}", startDate);

        LocalDate endDate = sessionService
                .getLocalDate(chatId, "endDate")
                .orElseThrow(() -> new DataNotFoundException("End date not found in session"));
        log.debug("Loaded from session: endDate={}", endDate);

        List<Car> availableCars = carService.getAvailableCarsByCategoryAndDates(carCategory, startDate, endDate);
        log.info("Fetched {} cars for category '{}'", availableCars.size(), carCategory);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarsKeyboard(availableCars);

        String text = String.format("""
                <b>Available cars in category '%s':</b>
                """, carCategory);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

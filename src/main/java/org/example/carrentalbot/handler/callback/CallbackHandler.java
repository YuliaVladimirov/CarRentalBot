package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.model.enums.FlowContext;

import java.util.EnumSet;


/**
 * Contract for handling Telegram inline keyboard interactions (callback queries).
 * <p>Each implementation processes a specific callback type identified by a unique
 * prefix in the callback data.</p>
 */
public interface CallbackHandler {

    /**
     * Returns the unique prefix used to match incoming callback data.
     * <p>The dispatcher uses this value to select the appropriate handler
     * when the callback data starts with the returned key.</p>
     *
     * @return handler key (callback data prefix)
     */
    String getKey();

    /**
     * Returns the flow contexts in which this handler is allowed to execute.
     * <p>Flow context prevents users from performing actions outside their current
     * step in the interaction workflow (e.g. calling a confirmation handler before starting a process).</p>
     *
     * @return allowed {@link FlowContext} values for execution
     */
    EnumSet<FlowContext> getAllowedContexts();

    /**
     * Processes the callback query.
     * <p>Typical implementations may update session state, validate input data,
     * persist changes, and send a response to the user.</p>
     *
     * @param chatId chat identifier where the callback originated
     * @param callbackQuery callback payload containing user and data information
     */
    void handle(Long chatId, CallbackQueryDto callbackQuery);
}

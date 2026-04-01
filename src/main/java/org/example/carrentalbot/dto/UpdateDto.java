package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an incoming update from the Telegram Bot API.
 * <p>An update can contain different types of user interactions such as a
 * standard message or a callback query triggered from inline buttons.</p>
 * <p>This DTO is used as the root payload for webhook processing.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDto {

    /**
     * Unique identifier of the update assigned by Telegram.
     */
    @JsonProperty("update_id")
    private Long updateId;

    /**
     * Incoming message data, present when the update is triggered by a user message.
     */
    @JsonProperty("message")
    private MessageDto message;

    /**
     * Callback query data, present when the update originates from an inline keyboard interaction.
     */
    @JsonProperty("callback_query")
    private CallbackQueryDto callbackQuery;
}

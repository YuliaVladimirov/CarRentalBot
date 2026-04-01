package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to send a message to a Telegram chat.
 * <p>Supports plain text or formatted messages, along with optional
 * inline keyboard markup for interactive user actions.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendMessageDto {

    /**
     * Identifier of the target chat.
     * <p>Used to deliver the message to a specific user, group, or channel.</p>
     */
    @JsonProperty("chat_id")
    private String chatId;

    /**
     * The message text to be sent.
     */
    @JsonProperty("text")
    private String text;

    /**
     * Optional parsing mode for text formatting (e.g., "HTML", "Markdown").
     */
    @JsonProperty("parse_mode")
    private String parseMode;

    /**
     * Optional inline keyboard markup attached to the message.
     * <p>Defines interactive buttons displayed below the message.</p>
     */
    @JsonProperty("reply_markup")
    private InlineKeyboardMarkupDto replyMarkup;
}

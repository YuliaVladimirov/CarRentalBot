package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to send a photo message to a Telegram chat.
 * <p>Supports attaching a caption, formatting the caption text, and
 * including an optional inline keyboard for user interaction.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendPhotoDto {

    /**
     * Identifier of the target chat.
     */
    @JsonProperty("chat_id")
    private String chatId;

    /**
     * Identifier or URL of the photo to be sent.
     * <p>Can be a file ID (for previously uploaded files) or an external URL.</p>
     */
    @JsonProperty("photo")
    private String photo;

    /**
     * Optional caption text displayed below the photo.
     */
    @JsonProperty("caption")
    private String caption;

    /**
     * Optional parsing mode for caption formatting (e.g., "HTML", "Markdown").
     */
    @JsonProperty("parse_mode")
    private String parseMode;

    /**
     * Optional inline keyboard attached to the message.
     */
    @JsonProperty("reply_markup")
    private InlineKeyboardMarkupDto replyMarkup;
}

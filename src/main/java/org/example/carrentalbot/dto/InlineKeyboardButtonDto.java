package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a single button within an inline keyboard.
 * <p>A button can trigger either a callback query (sent back to the bot)
 * or open an external URL. Only one action type should be defined per button.</p>
 */
@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InlineKeyboardButtonDto {

    /**
     * Text displayed on the button.
     */
    @JsonProperty("text")
    private String text;

    /**
     * Optional callback payload sent to the bot when the button is pressed.
     * <p>Used to identify and handle user actions within the application.</p>
     */
    @JsonProperty("callback_data")
    private String callbackData;

    /**
     * Optional URL opened when the button is pressed.
     */
    @JsonProperty("url")
    private String url;
}

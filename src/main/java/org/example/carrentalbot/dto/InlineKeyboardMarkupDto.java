package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents an inline keyboard attached to a Telegram message.
 * <p>An inline keyboard is defined as a two-dimensional list of buttons,
 * where each inner list represents a row of buttons displayed horizontally.</p>
 * <p>Used to provide interactive actions directly within the message.</p>
 */
@Data
@AllArgsConstructor
@Builder
public class InlineKeyboardMarkupDto {

    /**
     * Two-dimensional structure of buttons composing the keyboard layout.
     * <p>Each inner list represents a row of buttons.</p>
     */
    @JsonProperty("inline_keyboard")
    private List<List<InlineKeyboardButtonDto>> inlineKeyboard;
}

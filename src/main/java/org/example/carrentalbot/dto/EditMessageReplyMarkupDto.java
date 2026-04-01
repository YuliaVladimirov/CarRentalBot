package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to update the inline keyboard of an existing message.
 * <p>Used to modify or replace the interactive buttons of a previously sent
 * message without changing its text content.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditMessageReplyMarkupDto {

    /**
     * Identifier of the chat containing the target message.
     */
    @JsonProperty("chat_id")
    private Long chatId;

    /**
     * Identifier of the message whose inline keyboard should be updated.
     */
    @JsonProperty("message_id")
    private Integer messageId;

    /**
     * New inline keyboard markup to attach to the message.
     * <p>Replaces the existing keyboard.</p>
     */
    @JsonProperty("reply_markup")
    private InlineKeyboardMarkupDto replyMarkup;
}

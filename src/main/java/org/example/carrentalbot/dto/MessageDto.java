package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a user-generated message received from Telegram.
 * <p>Contains metadata about the sender, chat context, message content,
 * and the time the message was sent.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    /**
     * Unique identifier of the message within the chat.
     */
    @JsonProperty("message_id")
    private Integer messageId;

    /**
     * Information about the sender of the message.
     */
    @JsonProperty("from")
    private FromDto from;

    /**
     * Chat context in which the message was sent.
     */
    @JsonProperty("chat")
    private ChatDto chat;

    /**
     * Text content of the message.
     * <p>May be {@code null} for non-text messages.</p>
     */
    @JsonProperty("text")
    private String text;

    /**
     * Unix timestamp (in seconds) indicating when the message was sent.
     */
    @JsonProperty("date")
    private Long date;
}

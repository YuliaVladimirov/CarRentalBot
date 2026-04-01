package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the chat context in which a message or interaction occurs.
 * <p>Identifies the conversation (private, group, or channel) and provides
 * the target identifier used for sending responses back to Telegram.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDto {

    /**
     * Unique identifier of the chat.
     * <p>Used as the target when sending messages to this conversation.</p>
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Type of the chat (e.g., "private", "group", "supergroup", "channel").
     */
    @JsonProperty("type")
    private String type;
}

package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    @JsonProperty("message_id")
    private Long messageId;

    @JsonProperty("from")
    private FromDto from;

    @JsonProperty("chat")
    private ChatDto chat;

    @JsonProperty("text")
    private String text;

    @JsonProperty("date")
    private Long date; // Telegram uses UNIX timestamp
}

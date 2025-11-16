package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditMessageReplyMarkupDto {

    @JsonProperty("chat_id")
    private Long chatId;

    @JsonProperty("message_id")
    private Integer messageId;

    @JsonProperty("reply_markup")
    private InlineKeyboardMarkupDto replyMarkup;
}

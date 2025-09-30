package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class InlineKeyboardButtonDto {

    @JsonProperty("text")
    private String text;

    @JsonProperty("callback_data")
    private String callbackData;
}

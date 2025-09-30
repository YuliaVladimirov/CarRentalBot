package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class InlineKeyboardMarkupDto {

    @JsonProperty("inline_keyboard")
    private List<List<InlineKeyboardButtonDto>> inlineKeyboard;
}

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
public class UpdateDto {

    @JsonProperty("update_id")
    private Long updateId;

    @JsonProperty("message")
    private MessageDto message;

    @JsonProperty("callback_query")
    private CallbackQueryDto callbackQuery;
}

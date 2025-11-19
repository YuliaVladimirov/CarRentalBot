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
public class CallbackQueryDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("from")
    private FromDto from;

    @JsonProperty("message")
    private MessageDto message;

    @JsonProperty("data")
    private String data;
}

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
public class ChatDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("type")
    private String type; // "private", "group", etc.
}

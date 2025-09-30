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
public class AnswerCallbackQueryDto {

    @JsonProperty("callback_query_id")
    private String callbackQueryId;

    @JsonProperty("text")
    private String text;          // optional

    @JsonProperty("show_alert")
    private Boolean showAlert;   // optional (if true, shows alert instead of toast)

    @JsonProperty("url")
    private String url;           // optional (open a URL)

    @JsonProperty("cache_time")
    private Integer cacheTime;   // optional
}
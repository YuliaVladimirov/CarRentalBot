package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a response to a callback query triggered by an inline keyboard interaction.
 * <p>Used to notify the user that their action has been processed. The response
 * can display a brief notification, an alert popup, or optionally redirect
 * the user to a URL.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnswerCallbackQueryDto {

    /**
     * Identifier of the callback query to be answered.
     */
    @JsonProperty("callback_query_id")
    private String callbackQueryId;

    /**
     * Optional notification text displayed to the user.
     * <p>If {@code showAlert} is {@code false}, the text is shown as a brief toast message.</p>
     */
    @JsonProperty("text")
    private String text;

    /**
     * If {@code true}, displays the response as a modal alert instead of a toast notification.
     */
    @JsonProperty("show_alert")
    private Boolean showAlert;

    /**
     * Optional URL to open when the user interacts with the notification.
     */
    @JsonProperty("url")
    private String url;

    /**
     * Maximum time in seconds that the result of the callback query may be cached.
     */
    @JsonProperty("cache_time")
    private Integer cacheTime;
}
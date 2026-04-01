package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a callback query triggered by a user interaction with
 * an inline keyboard button.
 * <p>Contains information about the user who initiated the action,
 * the originating message, and the callback payload used to identify
 * the requested operation.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallbackQueryDto {

    /**
     * Unique identifier of the callback query.
     */
    @JsonProperty("id")
    private String id;

    /**
     * Information about the user who triggered the callback.
     */
    @JsonProperty("from")
    private FromDto from;

    /**
     * The message associated with the inline keyboard that generated the callback.
     */
    @JsonProperty("message")
    private MessageDto message;

    /**
     * Callback payload data used to identify the action to be performed.
     * <p>Typically contains encoded command or state information.</p>
     */
    @JsonProperty("data")
    private String data;
}

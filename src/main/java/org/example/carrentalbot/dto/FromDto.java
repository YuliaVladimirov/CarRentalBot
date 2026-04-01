package org.example.carrentalbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Telegram user involved in an incoming update.
 * <p>Contains basic profile information about the sender, such as identifiers
 * and display names. This object is used in both messages and callback queries.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FromDto {

    /**
     * Unique identifier of the user.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Indicates whether the sender is a bot account.
     */
    @JsonProperty("is_bot")
    private Boolean isBot;

    /**
     * User's first name as provided by Telegram.
     */
    @JsonProperty("first_name")
    private String firstName;

    /**
     * User's last name as provided by Telegram.
     */
    @JsonProperty("last_name")
    private String lastName;

    /**
     * Optional username associated with the user.
     */
    @JsonProperty("username")
    private String userName;
}

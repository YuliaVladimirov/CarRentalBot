package org.example.carrentalbot.aop;

import lombok.Getter;

@Getter
public enum MDCFields {

    TRACE_ID ("trace_id"),

    UPDATE_ID ("update_id"),
    UPDATE_TYPE ("update_type"),
    CHAT_ID ("chat_id"),
    TELEGRAM_USER_ID ("telegram_user_id"),

    CLASS ("class"),
    METHOD ("method");

    private final String value;

    MDCFields (String value) {
        this.value = value;
    }
}
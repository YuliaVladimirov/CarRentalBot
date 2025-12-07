package org.example.carrentalbot.aop;

import lombok.Getter;

@Getter
public enum MDCFields {

    TRACE_ID ("traceId"),

    UPDATE_ID ("updateId"),
    UPDATE_TYPE ("updateType"),
    CHAT_ID ("chatId"),
    TELEGRAM_USER_ID ("telegramUserId"),

    CLASS ("class"),
    METHOD ("method");

    private final String value;

    MDCFields (String value) {
        this.value = value;
    }
}
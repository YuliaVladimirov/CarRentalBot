package org.example.carrentalbot.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.carrentalbot.dto.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.example.carrentalbot.aop.MDCFields.*;

@Slf4j
@Aspect
@Component
public class TracingAspect {

    private static final String POINTCUT_EXPRESSION =
            "@within(org.springframework.web.bind.annotation.RestController) || " +
                    "@within(org.springframework.stereotype.Service) || " +
                    "@annotation(org.springframework.scheduling.annotation.Async)";

    private Long extractUpdateId(UpdateDto update) {
        return update.getUpdateId();
    }

    private String extractUpdateType(UpdateDto update) {
        return Optional.ofNullable(update.getMessage()).map(m -> "message")
                .orElseGet(() ->
                        Optional.ofNullable(update.getCallbackQuery()).map(c -> "callbackQuery")
                                .orElse("other"));
    }

    private Long extractChatId(UpdateDto update) {
        return Optional.ofNullable(update.getMessage())
                .map(MessageDto::getChat)
                .map(ChatDto::getId)
                .or(() ->
                        Optional.ofNullable(update.getCallbackQuery())
                                .map(CallbackQueryDto::getMessage)
                                .map(MessageDto::getChat)
                                .map(ChatDto::getId)
                )
                .orElse(null);
    }

    private Long extractTelegramUserId(UpdateDto update) {
        return Optional.ofNullable(update.getMessage())
                .map(MessageDto::getFrom)
                .map(FromDto::getId)
                .or(() ->
                        Optional.ofNullable(update.getCallbackQuery())
                                .map(CallbackQueryDto::getFrom)
                                .map(FromDto::getId)
                )
                .orElse(null);
    }

    @Around(POINTCUT_EXPRESSION)
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        MDC.put(CLASS.getValue(), className);
        MDC.put(METHOD.getValue(), methodName);
        MDC.put(TRACE_ID.getValue(), UUID.randomUUID().toString());

        Object[] args = joinPoint.getArgs();

        if (className.endsWith("Controller")) {
            UpdateDto update = (UpdateDto) Arrays.stream(args)
                    .filter(arg -> arg instanceof UpdateDto)
                    .findFirst()
                    .orElse(null);

            if (update != null) {

                try {
                    Long updateId = extractUpdateId(update);
                    String updateType = extractUpdateType(update);
                    Long chatId = extractChatId(update);
                    Long telegramUserId = extractTelegramUserId(update);

                    if (updateId != null) MDC.put(UPDATE_ID.getValue(), updateId.toString());
                    MDC.put(UPDATE_TYPE.getValue(), updateType);
                    if (chatId != null) MDC.put(CHAT_ID.getValue(), chatId.toString());
                    if (telegramUserId != null) MDC.put(TELEGRAM_USER_ID.getValue(), telegramUserId.toString());
                } catch (Exception e) {
                    log.warn("Failed to extract Telegram context IDs from update: {}", e.getMessage());
                }
            }
        }

        try {

            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Completed in {} ms", executionTime);

            return result;

        } catch (Throwable exception) {

            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[ERROR] | executionTime={}| message={}", executionTime, exception.getMessage(), exception);

            throw exception;
        }
    }
}

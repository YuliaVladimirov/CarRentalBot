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
                    "@annotation(org.springframework.scheduling.annotation.Async) || ";

    private Long extractUpdateId(UpdateDto update) {
        return update.getUpdateId();
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

        Object[] args = joinPoint.getArgs();

        if (className.endsWith("Controller")) {
            UpdateDto update = (UpdateDto) Arrays.stream(args)
                    .filter(arg -> arg instanceof UpdateDto)
                    .findFirst()
                    .orElse(null);

            if (update != null) {
                if (MDC.get(REQUEST_ID.name()) == null) {
                    MDC.put(REQUEST_ID.name(), UUID.randomUUID().toString());
                }

                try {
                    Long updateId = extractUpdateId(update);
                    Long chatId = extractChatId(update);
                    Long telegramUserId = extractTelegramUserId(update);

                    if (updateId != null) MDC.put(UPDATE_ID.name(), String.valueOf(updateId));
                    if (chatId != null) MDC.put(CHAT_ID.name(), String.valueOf(chatId));
                    if (telegramUserId != null) MDC.put(TELEGRAM_USER_ID.name(), String.valueOf(telegramUserId));
                } catch (Exception e) {
                    log.warn("Failed to extract Telegram context IDs from update: {}", e.getMessage());
                }
            }
        }

        String prevClass = MDC.get(CLASS.name());
        String prevMethod = MDC.get(METHOD.name());

        MDC.put(CLASS.name(), className);
        MDC.put(METHOD.name(), methodName);

        try {
            log.debug("[START] | args={}", Arrays.toString(args));

            Object result = joinPoint.proceed();
            Object resultObject = result != null ? result : "void/null";
            long duration = System.currentTimeMillis() - startTime;

            log.info("duration={}", duration);
            log.debug("[END] | duration={}| result={}", duration, resultObject);

            return result;

        } catch (Throwable exception) {

            long duration = System.currentTimeMillis() - startTime;
            log.error("[ERROR] | duration={}| message={}", duration, exception.getMessage(), exception);

            throw exception;

        } finally {

            if (prevClass != null) MDC.put(CLASS.name(), prevClass);
            else MDC.remove(CLASS.name());

            if (prevMethod != null) MDC.put(METHOD.name(), prevMethod);
            else MDC.remove(METHOD.name());
        }
    }
}

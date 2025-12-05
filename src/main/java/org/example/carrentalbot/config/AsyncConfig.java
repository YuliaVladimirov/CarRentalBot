package org.example.carrentalbot.config;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.exception.CustomAsyncExceptionHandler;
import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

    //Custom handler
    private final CustomAsyncExceptionHandler customAsyncExceptionHandler;

    static class MdcTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    } else {
                        MDC.clear();
                    }
                    MDC.put("thread", Thread.currentThread().getName());
                    runnable.run();
                } finally {
                    if (previous != null) {
                        MDC.setContextMap(previous);
                    } else {
                        MDC.clear();
                    }
                }
            };
        }
    }


    // Executor for Telegram bot async tasks (processing updates, DB work, etc.)
    @Bean(name = "telegramExecutor")
    @Primary
    public Executor telegramExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);     // minimum threads always alive
        executor.setMaxPoolSize(50);      // maximum threads for bursts
        executor.setQueueCapacity(500);   // pending updates queue
        executor.setThreadNamePrefix("TelegramThread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    // Executor dedicated to sending emails
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);      // minimum threads always alive
        executor.setMaxPoolSize(5);       // maximum threads for bursts
        executor.setQueueCapacity(20);    // small queue for pending emails
        executor.setThreadNamePrefix("EmailThread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    // This method registers the custom handler with all task executors
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return customAsyncExceptionHandler;
    }

    @Override
    public Executor getAsyncExecutor() {
        return telegramExecutor();
    }
}

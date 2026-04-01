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

/**
 * Configuration class for application asynchronous execution.
 * <p>Defines thread pool executors for different types of background tasks
 * such as Telegram processing and email sending.</p>
 * <p>Also integrates MDC (Mapped Diagnostic Context) propagation across threads
 * to preserve logging context in asynchronous execution.</p>
 */
@Configuration
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

    //Custom handler
    private final CustomAsyncExceptionHandler customAsyncExceptionHandler;

    /**
     * Task decorator responsible for propagating MDC context across threads.
     * <p>This ensures that logging context (such as request identifiers or thread names)
     * is preserved when tasks are executed asynchronously.</p>
     */
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

    /**
     * Executor for Telegram-related asynchronous tasks.
     * <p>Used for processing bot updates, handling business logic, and database operations.
     * Configured with a large thread pool to handle high load bursts.</p>
     *
     * @return configured {@link Executor} for Telegram processing
     */
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

    /**
     * Executor dedicated to email sending tasks.
     * <p>Designed for lightweight background email processing with limited concurrency
     * to prevent overload of external email providers.</p>
     *
     * @return configured {@link Executor} for email processing
     */
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

    /**
     * Provides a custom handler for uncaught exceptions in asynchronous methods.
     * <p>This handler is applied to all async executors to ensure proper logging
     * and error tracking for background tasks.</p>
     *
     * @return custom {@link AsyncUncaughtExceptionHandler}
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return customAsyncExceptionHandler;
    }

    /**
     * Defines the default async executor used by Spring's {@code @Async} methods.
     * <p>Delegates execution to the Telegram executor by default.</p>
     *
     * @return default async {@link Executor}
     */
    @Override
    public Executor getAsyncExecutor() {
        return telegramExecutor();
    }
}

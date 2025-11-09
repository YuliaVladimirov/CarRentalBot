package org.example.carrentalbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    // Executor for Telegram bot async tasks (processing updates, DB work, etc.)
    @Bean(name = "telegramExecutor")
    public Executor telegramExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);     // minimum threads always alive
        executor.setMaxPoolSize(50);      // maximum threads for bursts
        executor.setQueueCapacity(500);   // pending updates queue
        executor.setThreadNamePrefix("TelegramThread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
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
        executor.initialize();
        return executor;
    }
}

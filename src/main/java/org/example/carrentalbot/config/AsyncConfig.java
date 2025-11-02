package org.example.carrentalbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);     // min active threads
        executor.setMaxPoolSize(50);      // max threads for spikes
        executor.setQueueCapacity(500);   // pending updates queue
        executor.setThreadNamePrefix("TelegramBot-");
        executor.initialize();
        return executor;
    }
}

package org.example.carrentalbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Application-wide configuration class.
 * <p>Defines shared infrastructure beans used across the application.</p>
 */
@Configuration
public class AppConfig {

    /**
     * Provides a {@link RestTemplate} bean for performing synchronous HTTP requests.
     * <p>This bean is used for communication with external APIs such as Telegram
     * and other third-party services.</p>
     *
     * @return configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

package com.werner.battery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final BatteryProperties batteryProperties;

    public WebClientConfig(BatteryProperties batteryProperties) {
        this.batteryProperties = batteryProperties;
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://enlighten.enphaseenergy.com")
                .defaultHeader("e-auth-token", batteryProperties.eAuthToken())
                .build();
    }
}

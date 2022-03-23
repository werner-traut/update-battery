package com.werner.battery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableConfigurationProperties(BatteryProperties.class)
@EnableScheduling
@Slf4j
public class BatteryApplication {


    public static final int NEW_RESERVE_MORNING = 10;
    public static final int NEW_RESERVE_NIGHT = 45;

    private final WebClient webClient;

    public BatteryApplication(WebClient webClient) {
        this.webClient = webClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(BatteryApplication.class, args);
    }

    @Scheduled(cron = "0 45 5 * * MON-FRI", zone = "US/Arizona")
    public void updateBatteryMorning() {
        updateReserve(NEW_RESERVE_MORNING);
    }

    @Scheduled(cron = "0 0 21 * * MON-FRI", zone = "US/Arizona")
    public void updateBatteryNight() {
        updateReserve(NEW_RESERVE_NIGHT);
    }

    private void updateReserve(int newReserve) {
        log.info("Updating battery reserve to {}", newReserve);

        ResponseEntity<BatteryResponse> response = null;
        try {
            response = webClient
                    .put()
                    .uri("/pv/settings/2537485/battery_config?usage=self-consumption&battery_backup_percentage=" + newReserve + "&operation_mode_sub_type=")
                    .retrieve()
                    .toEntity(BatteryResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error updating battery reserve to {}", newReserve, e);
            throw e;
        }

        log.info("Response received: {}, with status code: {}", response.getBody().message(), response.getStatusCode());
    }

    record BatteryResponse(String message) {}

}

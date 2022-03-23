package com.werner.battery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableConfigurationProperties(BatteryProperties.class)
@EnableScheduling
@Slf4j
public class BatteryApplication {

    @Value("${battery.morning-reserve}")
    private String NEW_RESERVE_MORNING;
    @Value("${battery.day-reserve}")
    private String NEW_RESERVE_DAY;
    @Value("${battery.night-reserve}")
    private String NEW_RESERVE_NIGHT;

    private final WebClient webClient;
    private final BatteryProperties batteryProperties;

    public BatteryApplication(WebClient webClient, BatteryProperties batteryProperties) {
        this.webClient = webClient;
        this.batteryProperties = batteryProperties;
    }

    public static void main(String[] args) {
        SpringApplication.run(BatteryApplication.class, args);
    }

    @Scheduled(cron = "${battery.morning-cron}", zone = "US/Arizona")
    public void updateBatteryMorning() {
        if (batteryProperties.morningEnabled()) {
            updateReserve(Integer.parseInt(NEW_RESERVE_MORNING));
        }
    }

    @Scheduled(cron = "${battery.day-cron}", zone = "US/Arizona")
    public void updateBatteryDay() {
        if (batteryProperties.dayEnabled()) {
            updateReserve(Integer.parseInt(NEW_RESERVE_DAY));
        }
    }

    @Scheduled(cron = "${battery.night-cron}", zone = "US/Arizona")
    public void updateBatteryNight() {
        if (batteryProperties.nightEnabled()) {
            updateReserve(Integer.parseInt(NEW_RESERVE_NIGHT));
        }
    }

    @Scheduled(fixedRate = 1000 * 60 * 25) //25 minutes
    public void keepAppUp() {
        HttpStatus statusCode = webClient.get()
                .uri("https://update-battery.herokuapp.com/actuator/health")
//                .uri("http://localhost:8080/actuator/health")
                .retrieve()
                .toEntity(Void.class)
                .block()
                .getStatusCode();
        log.info("Pinged app and got {} status code", statusCode);
    }

    private void updateReserve(int newReserve) {
        log.info("Updating battery reserve to {}", newReserve);

        ResponseEntity<BatteryResponse> response = null;
        try {
            response = webClient
                    .put()
                    .uri("https://enlighten.enphaseenergy.com/pv/settings/2537485/battery_config?usage=self-consumption&battery_backup_percentage=" + newReserve + "&operation_mode_sub_type=")
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

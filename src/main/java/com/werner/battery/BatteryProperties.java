package com.werner.battery;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "battery")
public record BatteryProperties(String eAuthToken,
                                Integer morningReserve,
                                Integer dayReserve,
                                Integer nightReserve,
                                String morningCron,
                                String dayCron,
                                String nightCron,
                                Boolean morningEnabled,
                                Boolean dayEnabled,
                                Boolean nightEnabled
                                ) {
}

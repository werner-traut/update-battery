package com.werner.battery;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "battery")
public record BatteryProperties(String eAuthToken) {
}

package com.homestay.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    /** Injected clock so "today" on the dashboard is testable and uses Indian Standard Time. */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Kolkata"));
    }
}

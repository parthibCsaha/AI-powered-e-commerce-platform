package com.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitConfig {

    private TierConfig auth = new TierConfig(1, 1);
    private TierConfig ai = new TierConfig(5, 1);
    private TierConfig general = new TierConfig(50, 1);

    @Getter
    @Setter
    public static class TierConfig {
        private int capacity;
        private int minutes;

        public TierConfig() {}

        public TierConfig(int capacity, int minutes) {
            this.capacity = capacity;
            this.minutes = minutes;
        }
    }
}

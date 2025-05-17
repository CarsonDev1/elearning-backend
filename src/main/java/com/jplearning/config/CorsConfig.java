package com.jplearning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow all origins
        config.addAllowedOrigin("*");

        // Allow all headers
        config.addAllowedHeader("*");

        // Allow all methods
        config.addAllowedMethod("*");

        // Allow credentials (cookies, authentication)
        config.setAllowCredentials(false);

        // Apply this configuration to all paths
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
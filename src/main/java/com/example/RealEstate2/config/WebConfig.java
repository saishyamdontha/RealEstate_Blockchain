package com.example.RealEstate2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Local dev only: Vite's dev server runs on a different port than
        // the Spring Boot API, so the browser treats it as cross-origin.
        // Restricted to localhost:5173 rather than a wildcard.
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "https://real-estate-blockchain-lake.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}

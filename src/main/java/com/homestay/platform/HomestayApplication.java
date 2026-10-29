package com.homestay.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point. Extends SpringBootServletInitializer so the WAR can be dropped
 * into an external Tomcat 10.1 (Task 8) and also run with `java -jar homestay.war`
 * (local development and Docker, Tasks 3 and 11).
 */
@SpringBootApplication
public class HomestayApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(HomestayApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(HomestayApplication.class);
    }
}

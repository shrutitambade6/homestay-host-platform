package com.homestay.platform.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds the build version and environment to every page (shown in the footer). */
@ControllerAdvice
public class GlobalModelAttributes {

    private final String version;
    private final String environment;

    public GlobalModelAttributes(@Value("${app.version:dev}") String version,
                                 @Value("${app.environment:dev}") String environment) {
        this.version = version;
        this.environment = environment;
    }

    @ModelAttribute("appVersion")
    public String appVersion() {
        return version;
    }

    @ModelAttribute("appEnv")
    public String appEnv() {
        return environment;
    }
}

package com.vaishnavi.purchase_decision_api.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * "/" now serves the web app (static/index.html, handled by Spring Boot's default
 * resource chain), so the only thing left to wire is a friendly shortcut to the
 * interactive API docs.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/docs", "/swagger-ui/index.html");
    }
}

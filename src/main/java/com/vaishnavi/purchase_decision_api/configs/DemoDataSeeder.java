package com.vaishnavi.purchase_decision_api.configs;

import com.vaishnavi.purchase_decision_api.service.DemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Puts the shared demo account in place on every boot so the credentials printed in
 * the Swagger docs always work. Turn the whole demo feature off with
 * {@code app.demo.enabled=false}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private final DemoService demoService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            demoService.createOrResetSharedAccount();
            log.info("Demo account ready: {} / {}",
                    DemoService.SHARED_DEMO_EMAIL, DemoService.DEMO_PASSWORD);
        } catch (Exception ex) {
            // Seeding is a convenience — never let it stop the app from starting.
            log.warn("Could not seed the demo account: {}", ex.getMessage());
        }
    }
}

package com.example.demosass.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupPortLogger implements ApplicationListener<WebServerInitializedEvent> {

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        log.info("========================================");
        log.info("  ServiLink API corriendo en puerto: {}", port);
        log.info("  http://localhost:{}/api", port);
        log.info("========================================");
    }
}

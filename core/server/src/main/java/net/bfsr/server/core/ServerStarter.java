package net.bfsr.server.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ServerStarter {
    @EventListener
    public void event(ApplicationReadyEvent event) {
        SpringApplication springApplication = event.getSpringApplication();
        Class<?> aClass = springApplication.getMainApplicationClass();
        GameServerSpringApplication annotation = aClass.getDeclaredAnnotation(GameServerSpringApplication.class);
        if (annotation != null) {
            Server server = event.getApplicationContext().getBean(Server.class);
            server.setLocal(annotation.type() == ServerType.LOCAL);
            server.run();
        }
    }

    @Bean
    public Server getServer() {
        return new Server();
    }
}
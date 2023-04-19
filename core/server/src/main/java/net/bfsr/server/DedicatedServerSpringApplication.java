package net.bfsr.server;

import net.bfsr.server.core.GameServerSpringApplication;
import net.bfsr.server.core.ServerType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@GameServerSpringApplication(type = ServerType.DEDICATED)
public class DedicatedServerSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(DedicatedServerSpringApplication.class, args);
    }
}
package net.bfsr.server;

import net.bfsr.server.core.GameServerSpringApplication;
import net.bfsr.server.core.ServerType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@GameServerSpringApplication(type = ServerType.LOCAL)
public class LocalServerSpringApplication {
    public static void main() {
        SpringApplication.run(LocalServerSpringApplication.class);
    }
}
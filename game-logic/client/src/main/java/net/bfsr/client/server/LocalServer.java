package net.bfsr.client.server;

import lombok.extern.log4j.Log4j2;
import net.bfsr.server.Server;

@Log4j2
public class LocalServer extends Server {
    public LocalServer() {
        super(LocalServerGameLogic.class);
    }

    @Override
    public void run() {
        log.info("Starting local server...");
        super.run();
    }
}
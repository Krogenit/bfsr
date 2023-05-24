package net.bfsr.client.server;

import lombok.extern.log4j.Log4j2;
import net.bfsr.server.Server;
import net.bfsr.server.ServerGameLogic;

@Log4j2
public class LocalServer extends Server {
    public LocalServer(ServerGameLogic gameLogic) {
        super(gameLogic);
    }

    @Override
    public void run() {
        log.info("Starting local server...");
        super.run();
    }
}
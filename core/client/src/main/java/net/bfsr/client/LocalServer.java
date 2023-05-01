package net.bfsr.client;

import lombok.extern.log4j.Log4j2;
import net.bfsr.server.core.Server;
import net.bfsr.server.player.Player;

@Log4j2
public class LocalServer extends Server {
    @Override
    protected void updateWorld() {
        if (!pause) super.updateWorld();
    }

    @Override
    public void run() {
        log.info("Starting local server...");
        super.run();
    }

    @Override
    protected void loadConfigs() {

    }

    @Override
    public void onPlayerDisconnected(Player player) {
        super.onPlayerDisconnected(player);
        log.info("Stopping local server");
        stop();
    }
}
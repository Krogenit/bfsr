package net.bfsr.client.server;

import lombok.extern.log4j.Log4j2;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.player.Player;

@Log4j2
public class LocalServerGameLogic extends ServerGameLogic {
    @Override
    protected void updateWorld() {
        if (!isPaused()) super.updateWorld();
    }

    @Override
    protected void loadConfigs() {}

    @Override
    public void onPlayerDisconnected(Player player) {
        super.onPlayerDisconnected(player);
        log.info("Stopping local server");
        stop();
    }
}
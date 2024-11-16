package net.bfsr.client.server;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.player.Player;

@Log4j2
public class LocalServerGameLogic extends ServerGameLogic {
    public LocalServerGameLogic(Profiler profiler) {
        super(profiler);
    }

    @Override
    protected void updateWorld(double time) {
        if (!isPaused()) super.updateWorld(time);
    }

    @Override
    public void onPlayerDisconnected(Player player) {
        super.onPlayerDisconnected(player);
        log.info("Stopping local server");
        stop();
    }
}
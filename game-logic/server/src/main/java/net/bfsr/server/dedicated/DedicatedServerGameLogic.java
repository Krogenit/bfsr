package net.bfsr.server.dedicated;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.player.PlayerManager;

@Log4j2
public class DedicatedServerGameLogic extends ServerGameLogic {
    public DedicatedServerGameLogic(Profiler profiler) {
        super(profiler);
    }

    @Override
    protected ServerSettings createSettings() {
        return ServerSettings.load();
    }

    @Override
    protected PlayerManager createPlayerManager() {
        return new DedicatedPlayerManager();
    }

    @Override
    public void setPaused(boolean pause) {}
}
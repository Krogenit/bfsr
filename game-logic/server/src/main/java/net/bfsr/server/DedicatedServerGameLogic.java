package net.bfsr.server;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.server.config.ServerSettings;

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
    public void setPaused(boolean pause) {}
}
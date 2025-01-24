package net.bfsr.server.dedicated;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.database.PlayerRepository;
import net.bfsr.server.database.RemotePlayerRepository;

@Log4j2
public class DedicatedServerGameLogic extends ServerGameLogic {
    public DedicatedServerGameLogic(Profiler profiler, EventBus eventBus) {
        super(profiler, eventBus);
    }

    @Override
    protected ServerSettings createSettings() {
        return ServerSettings.load();
    }

    @Override
    protected PlayerRepository createPlayerRepository(ServerSettings settings) {
        return new RemotePlayerRepository(settings);
    }

    @Override
    public void setPaused(boolean pause) {}
}
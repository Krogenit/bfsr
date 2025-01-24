package net.bfsr.client.server;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.database.FileSystemPlayerRepository;
import net.bfsr.server.database.PlayerRepository;

@Log4j2
public class LocalServerGameLogic extends ServerGameLogic {
    public LocalServerGameLogic(Profiler profiler, EventBus eventBus) {
        super(profiler, eventBus);
    }

    @Override
    protected PlayerRepository createPlayerRepository(ServerSettings settings) {
        return new FileSystemPlayerRepository();
    }

    @Override
    protected void updateWorld(double time) {
        if (!isPaused()) {
            super.updateWorld(time);
        }
    }
}
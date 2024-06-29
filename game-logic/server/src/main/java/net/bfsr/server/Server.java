package net.bfsr.server;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.loop.AbstractGameLoop;
import net.bfsr.engine.profiler.Profiler;

@Log4j2
@AllArgsConstructor
public abstract class Server extends AbstractGameLoop {
    protected final ServerGameLogic gameLogic;
    private final Profiler profiler = new Profiler();

    @Override
    public void run() {
        log.info("Server initialization...");
        init();
        log.info("Initialized");
        super.run();
    }

    protected void init() {
        gameLogic.init();
    }

    @Override
    public void update(double time) {
        profiler.start("update");
        gameLogic.update(time);
        profiler.end();
    }

    @Override
    public void setFps(int fps) {
        gameLogic.setFps(fps);
    }

    @Override
    public boolean isRunning() {
        return gameLogic.isRunning();
    }

    @Override
    public void clear() {
        super.clear();
        gameLogic.clear();
    }

    public void stop() {
        gameLogic.stop();
    }

    @Override
    protected int getUpdatesPerSecond() {
        return gameLogic.getUpdatesPerSecond();
    }

    @Override
    protected float getUpdateDeltaTime() {
        return gameLogic.getUpdateDeltaTime();
    }

    @Override
    protected double getTimeBetweenUpdates() {
        return gameLogic.getTimeBetweenUpdates();
    }
}
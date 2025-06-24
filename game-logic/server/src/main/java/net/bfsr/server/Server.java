package net.bfsr.server;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.loop.AbstractGameLoop;
import net.bfsr.engine.profiler.Profiler;

@Log4j2
public abstract class Server extends AbstractGameLoop {
    protected final ServerGameLogic gameLogic;
    private final Profiler profiler = new Profiler();

    protected Server(Class<? extends ServerGameLogic> gameLogicClass) {
        try {
            gameLogic = gameLogicClass.getConstructor(AbstractGameLoop.class, Profiler.class, EventBus.class)
                    .newInstance(this, profiler, new EventBus());
        } catch (Exception e) {
            throw new RuntimeException("Couldn't create game logic instance", e);
        }
    }

    @Override
    public void run() {
        log.info("Server initialization...");
        init();
        gameLogic.setRunning(true);
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
        gameLogic.shutdown();
    }
}
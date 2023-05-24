package net.bfsr.server;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.Engine;
import net.bfsr.engine.loop.AbstractLoop;
import net.bfsr.engine.util.Side;

@Log4j2
@AllArgsConstructor
public abstract class Server extends AbstractLoop {
    protected final ServerGameLogic gameLogic;

    @Override
    public void run() {
        log.info("Server initialization...");
        init();
        log.info("Initialized");
        loop();
    }

    protected void init() {
        gameLogic.init();
        Engine.setGameLogic(Side.SERVER, gameLogic);
    }

    @Override
    public void update() {
        gameLogic.update();
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
}
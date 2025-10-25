package net.bfsr.server.engine;

import net.bfsr.engine.loop.AbstractGameLoop;

public class EmptyGameLoop extends AbstractGameLoop {
    @Override
    public void run() {}

    @Override
    public void update(int frame, double time) {}

    @Override
    public boolean isRunning() {
        return false;
    }
}

package net.bfsr.server.ai.task;

import net.bfsr.server.entity.Ship;

public abstract class AiTask {
    protected final Ship ship;

    protected AiTask(Ship ship) {
        this.ship = ship;
    }

    public abstract void execute();
    public abstract boolean shouldExecute();
}

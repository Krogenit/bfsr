package net.bfsr.ai.task;

import net.bfsr.entity.ship.Ship;

public abstract class AiTask {
    protected final Ship ship;

    public AiTask(Ship ship) {
        this.ship = ship;
    }

    public abstract void execute(double delta);
    public abstract boolean shouldExecute();
}

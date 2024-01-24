package net.bfsr.ai.task;

import net.bfsr.entity.ship.Ship;

public abstract class AiTask {
    protected Ship ship;

    public void init(Ship ship) {
        this.ship = ship;
    }

    public abstract void execute();
    public abstract boolean shouldExecute();
}
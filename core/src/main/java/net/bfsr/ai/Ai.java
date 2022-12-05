package net.bfsr.ai;

import net.bfsr.ai.task.AiTask;
import net.bfsr.entity.ship.Ship;

import java.util.ArrayList;
import java.util.List;

public class Ai {
    private final Ship ship;
    private final List<AiTask> tasks;

    private AiAggressiveType aggressiveType;

    public Ai(Ship ship) {
        this.ship = ship;
        this.tasks = new ArrayList<>();
    }

    public void addTask(AiTask task) {
        this.tasks.add(task);
    }

    public void update(double delta) {
        for (AiTask task : tasks) {
            if (task.shouldExecute())
                task.execute(delta);
        }
    }

    public void setAggressiveType(AiAggressiveType aggressiveType) {
        this.aggressiveType = aggressiveType;
    }

    public AiAggressiveType getAggressiveType() {
        return aggressiveType;
    }
}

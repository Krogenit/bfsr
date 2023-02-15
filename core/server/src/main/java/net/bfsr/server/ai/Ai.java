package net.bfsr.server.ai;

import net.bfsr.server.ai.task.AiTask;
import net.bfsr.server.entity.Ship;

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

    public void update() {
        for (int i = 0, tasksSize = tasks.size(); i < tasksSize; i++) {
            AiTask task = tasks.get(i);
            if (task.shouldExecute())
                task.execute();
        }
    }

    public void setAggressiveType(AiAggressiveType aggressiveType) {
        this.aggressiveType = aggressiveType;
    }

    public AiAggressiveType getAggressiveType() {
        return aggressiveType;
    }
}

package net.bfsr.ai;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.ai.task.AiTask;
import net.bfsr.entity.ship.Ship;

import java.util.ArrayList;
import java.util.List;

public class Ai {
    private final Ship ship;
    private final List<AiTask> tasks;

    @Getter
    @Setter
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
}
package net.bfsr.ai;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.ai.task.AiTask;
import net.bfsr.entity.ship.Ship;

import java.util.ArrayList;
import java.util.List;

public class Ai {
    public static final Ai NO_AI = new Ai() {
        {
            setAggressiveType(AiAggressiveType.NOTHING);
        }

        @Override
        public void addTask(AiTask task) {
            throw new RuntimeException("Can't add task to NO_AI field");
        }

        @Override
        public void update() {}
    };

    private final List<AiTask> tasks;

    @Getter
    @Setter
    private AiAggressiveType aggressiveType;

    public Ai() {
        this.tasks = new ArrayList<>();
    }

    public void init(Ship ship) {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).init(ship);
        }
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
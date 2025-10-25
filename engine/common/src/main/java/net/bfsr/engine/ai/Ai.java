package net.bfsr.engine.ai;

import net.bfsr.engine.ai.task.AiTask;
import net.bfsr.engine.world.entity.RigidBody;

import java.util.ArrayList;
import java.util.List;

public class Ai {
    public static final Ai NO_AI = new Ai() {
        @Override
        public void addTask(AiTask task) {
            throw new RuntimeException("Can't add task to NO_AI field");
        }

        @Override
        public void update() {
            // No execution without AI
        }
    };

    private final List<AiTask> tasks = new ArrayList<>();

    public void init(RigidBody rigidBody) {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).init(rigidBody);
        }
    }

    public void addTask(AiTask task) {
        this.tasks.add(task);
    }

    public void update() {
        for (int i = 0, tasksSize = tasks.size(); i < tasksSize; i++) {
            AiTask task = tasks.get(i);
            if (task.shouldExecute()) {
                task.execute();
            }
        }
    }
}
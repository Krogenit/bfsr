package net.bfsr.engine.ai.task;

import net.bfsr.engine.world.entity.RigidBody;

public abstract class AiTask {
    protected RigidBody rigidBody;

    public void init(RigidBody rigidBody) {
        this.rigidBody = rigidBody;
    }

    public abstract void execute();
    public abstract boolean shouldExecute();
}
package net.bfsr.engine.entity;

import lombok.Getter;
import net.bfsr.engine.Engine;

@Getter
public class SpawnAccumulator {
    private double accumulatedTime;

    public void resetTime() {
        accumulatedTime = 0.0f;
    }

    public void update() {
        accumulatedTime += Engine.getUpdateDeltaTime();
    }

    public void consume(double spawnTime) {
        accumulatedTime -= spawnTime;
    }
}
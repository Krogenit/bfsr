package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.Core;

@Getter
public class SpawnAccumulator {
    private final Core core = Core.get();
    private double accumulatedTime;

    public void resetTime() {
        accumulatedTime = 0.0f;
    }

    public void update() {
        accumulatedTime += core.getUpdateDeltaTime();
    }

    public void consume(double spawnTime) {
        accumulatedTime -= spawnTime;
    }
}
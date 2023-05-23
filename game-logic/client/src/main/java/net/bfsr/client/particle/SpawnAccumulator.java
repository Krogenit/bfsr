package net.bfsr.client.particle;

import lombok.Getter;

public class SpawnAccumulator {
    private long lastEmitTime;
    private long emitTime;
    @Getter
    private double accumulatedTime;

    public void resetTime() {
        lastEmitTime = emitTime = System.nanoTime();
        accumulatedTime = 0.0f;
    }

    public void update() {
        lastEmitTime = emitTime;
        emitTime = System.nanoTime();
        accumulatedTime += (emitTime - lastEmitTime) * 0.000000001;
    }

    public void consume(double spawnTime) {
        accumulatedTime -= spawnTime;
    }
}
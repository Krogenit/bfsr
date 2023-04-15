package net.bfsr.client.particle;

import lombok.Getter;

public class SpawnAccumulator {
    private long lastEmitTime;
    private long emitTime;
    @Getter
    private float accumulatedTime;

    public void resetTime() {
        lastEmitTime = emitTime = System.currentTimeMillis();
        accumulatedTime = 0.0f;
    }

    public void update() {
        lastEmitTime = emitTime;
        emitTime = System.currentTimeMillis();
        accumulatedTime += (emitTime - lastEmitTime) * 0.001f;
    }

    public void consume(float spawnTime) {
        accumulatedTime -= spawnTime;
    }
}
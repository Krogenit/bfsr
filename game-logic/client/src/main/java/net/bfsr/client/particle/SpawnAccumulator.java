package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.Client;

@Getter
public class SpawnAccumulator {
    private final Client client = Client.get();
    private double accumulatedTime;

    public void resetTime() {
        accumulatedTime = 0.0f;
    }

    public void update() {
        accumulatedTime += client.getUpdateDeltaTime();
    }

    public void consume(double spawnTime) {
        accumulatedTime -= spawnTime;
    }
}
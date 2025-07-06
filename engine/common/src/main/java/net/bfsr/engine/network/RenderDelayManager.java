package net.bfsr.engine.network;

import lombok.Getter;
import net.bfsr.engine.Engine;

public class RenderDelayManager {
    private static final float ADDITIONAL_DELAY_IN_MILLIS = 1.0f;
    private static final float PING_SMOOTH_FACTOR = 0.1f;

    private double maxPing = Engine.getClientRenderDelayInMills();
    @Getter
    private double renderDelayInNanos = maxPing * 1_000_000.0;
    @Getter
    private int renderDelayInFrames = Engine.convertMillisecondsToFrames(maxPing);

    public void addPingResult(double ping, double averagePing) {
        if (ping > maxPing) {
            maxPing = ping;
            updateDelay();
        } else {
            double pingDelta = maxPing - averagePing;
            if (pingDelta - 1.0 > PING_SMOOTH_FACTOR) {
                int count = (int) (pingDelta / PING_SMOOTH_FACTOR);
                if (count > 1) {
                    float diffValue = (PING_SMOOTH_FACTOR * count) / 2;
                    maxPing -= diffValue;
                } else {
                    maxPing -= PING_SMOOTH_FACTOR;
                }
                updateDelay();
            }
        }
    }

    private void updateDelay() {
        renderDelayInNanos = maxPing * 1_000_000.0 + ADDITIONAL_DELAY_IN_MILLIS;
        renderDelayInFrames = Engine.convertMillisecondsToFrames(maxPing) + 1;
    }

    public void reset() {
        maxPing = Engine.getClientRenderDelayInMills();
        renderDelayInNanos = maxPing * 1_000_000.0;
        renderDelayInFrames = Engine.convertMillisecondsToFrames(maxPing);
    }
}

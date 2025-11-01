package net.bfsr.engine.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.engine.RenderDelayChangeEvent;

@RequiredArgsConstructor
public class RenderDelayManager {
    private static final float PING_SMOOTH_FACTOR = 0.1f;

    private final EventBus eventBus;
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
        renderDelayInNanos = maxPing * 1_000_000.0;
        int lastRenderDelayInFrames = renderDelayInFrames;
        renderDelayInFrames = Engine.convertMillisecondsToFrames(maxPing) + 1;
        if (renderDelayInFrames != lastRenderDelayInFrames) {
            eventBus.publish(new RenderDelayChangeEvent(renderDelayInFrames));
        }
    }

    public void reset() {
        maxPing = Engine.getClientRenderDelayInMills();
        renderDelayInNanos = maxPing * 1_000_000.0;
        renderDelayInFrames = Engine.convertMillisecondsToFrames(maxPing);
    }
}

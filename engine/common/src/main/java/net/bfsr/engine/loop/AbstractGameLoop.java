package net.bfsr.engine.loop;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Setter
@Getter
public abstract class AbstractGameLoop implements GameLoop {
    private int updatesPerSecond = 60;
    private float updateDeltaTime = 1.0f / updatesPerSecond;
    private double timeBetweenUpdates = 1_000_000_000.0 / updatesPerSecond;

    @Override
    public void run() {
        final int maxUpdatesBeforeRender = 40;
        double lastUpdateTime = System.nanoTime();

        int lastSecondTime = (int) (lastUpdateTime / 1_000_000_000.0f);
        int framesCount = 0;

        try {
            while (isRunning()) {
                long now = System.nanoTime();

                int updateCount = 0;
                while (now - lastUpdateTime >= timeBetweenUpdates) {
                    lastUpdateTime += timeBetweenUpdates;
                    update(lastUpdateTime);

                    if (++updateCount == maxUpdatesBeforeRender) {
                        if (now - lastUpdateTime > timeBetweenUpdates) {
                            lastUpdateTime = now;
                        }

                        break;
                    }
                }

                render((float) ((now - lastUpdateTime) / timeBetweenUpdates));
                framesCount++;

                int thisSecond = (int) (lastUpdateTime / 1_000_000_000.0f);
                if (thisSecond > lastSecondTime) {
                    setFps(framesCount);
                    framesCount = 0;
                    lastSecondTime = thisSecond;
                }

                sync(now, lastUpdateTime);
            }
        } catch (RuntimeException e) {
            log.error("Exception in game loop", e);
        } finally {
            clear();
        }
    }

    protected void sync(long now, double lastUpdateTime) {
        while (now - lastUpdateTime < timeBetweenUpdates) {
            Thread.yield();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            now = System.nanoTime();
        }
    }

    @Override
    public void render(float interpolation) {}

    @Override
    public void setFps(int fps) {}

    @Override
    public void clear() {}
}
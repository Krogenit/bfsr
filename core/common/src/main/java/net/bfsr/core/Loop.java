package net.bfsr.core;

import net.bfsr.util.TimeUtils;

public class Loop extends AbstractLoop {
    private float timeBetweenUpdates;

    @Override
    protected void loop() {
        final int maxUpdatesBeforeRender = 4;
        double lastUpdateTime = System.nanoTime();

        int lastSecondTime = (int) (lastUpdateTime / 1000000000.0f);
        int frameCount = 0;

        timeBetweenUpdates = 1_000_000_000.0f / getUpdatesPerSecond();

        while (isRunning()) {
            long now = System.nanoTime();

            int updateCount = 0;

            while (now - lastUpdateTime > timeBetweenUpdates && updateCount < maxUpdatesBeforeRender) {
                update();
                lastUpdateTime += timeBetweenUpdates;
                updateCount++;
            }

            if (now - lastUpdateTime > timeBetweenUpdates) {
                lastUpdateTime = now;
            }

            float interpolation = Math.min(1.0f, (float) (now - lastUpdateTime) / timeBetweenUpdates);
            render(interpolation);
            onPostRender();

            frameCount++;

            int thisSecond = (int) (lastUpdateTime / 1000000000.0f);
            if (thisSecond > lastSecondTime) {
                setFps(frameCount);
                frameCount = 0;
                lastSecondTime = thisSecond;
            }

            sync(now, lastUpdateTime);
        }

        clear();
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
    protected void update() {}

    @Override
    protected void render(float interpolation) {}

    @Override
    protected void onPostRender() {}

    @Override
    protected void setFps(int fps) {}

    @Override
    protected int getUpdatesPerSecond() {
        return TimeUtils.UPDATES_PER_SECOND;
    }

    @Override
    protected void clear() {}
}
package net.bfsr.engine.loop;

import net.bfsr.engine.util.TimeUtils;

public abstract class AbstractLoop implements Loop {
    private float timeBetweenUpdates;

    @Override
    public void loop() {
        final int maxUpdatesBeforeRender = 40;
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
    public void update() {}

    @Override
    public void render(float interpolation) {}

    @Override
    public void onPostRender() {}

    @Override
    public void setFps(int fps) {}

    @Override
    public int getUpdatesPerSecond() {
        return TimeUtils.UPDATES_PER_SECOND;
    }

    @Override
    public void clear() {}
}
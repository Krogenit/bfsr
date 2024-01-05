package net.bfsr.engine.loop;

import net.bfsr.engine.util.TimeUtils;

public abstract class AbstractLoop implements Loop {
    private final float timeBetweenUpdates = 1_000_000_000.0f / getUpdatesPerSecond();

    @Override
    public void loop() {
        final int maxUpdatesBeforeRender = 40;
        double lastUpdateTime = System.nanoTime();

        int lastSecondTime = (int) (lastUpdateTime / 1_000_000_000.0f);
        int framesCount = 0;

        while (isRunning()) {
            long now = System.nanoTime();

            int updateCount = 0;
            while (now - lastUpdateTime >= timeBetweenUpdates) {
                update();
                lastUpdateTime += timeBetweenUpdates;

                if (++updateCount == maxUpdatesBeforeRender) {
                    if (now - lastUpdateTime > timeBetweenUpdates) {
                        lastUpdateTime = now;
                    }

                    break;
                }
            }

            render((float) (now - lastUpdateTime) / timeBetweenUpdates);
            framesCount++;

            int thisSecond = (int) (lastUpdateTime / 1_000_000_000.0f);
            if (thisSecond > lastSecondTime) {
                setFps(framesCount);
                framesCount = 0;
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
    public void setFps(int fps) {}

    @Override
    public int getUpdatesPerSecond() {
        return TimeUtils.UPDATES_PER_SECOND;
    }

    @Override
    public void clear() {}
}
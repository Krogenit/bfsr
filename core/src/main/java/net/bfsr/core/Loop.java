package net.bfsr.core;

import net.bfsr.util.TimeUtils;

public class Loop extends AbstractLoop {
    private float timeBetweenUpdates;

    @Override
    protected void loop() {
        // At the very most we will update the game this many times before a new render.
        // If you're worried about visual hitches more than perfect timing, set this to 1.
        final int maxUpdatesBeforeRender = 4;
        // We will need the last update time.
        double lastUpdateTime = System.nanoTime();
        long lastFrameTime = System.nanoTime();

        // Simple way of finding FPS.
        int lastSecondTime = (int) (lastUpdateTime / 1000000000.0f);
        int frameCount = 0;

        timeBetweenUpdates = 1_000_000_000.0f / getUpdatesPerSecond();

        while (isRunning()) {
            long now = System.nanoTime();

            int updateCount = 0;

            // Do as many game updates as we need to, potentially playing catchup.
            while (now - lastUpdateTime > timeBetweenUpdates && updateCount < maxUpdatesBeforeRender) {
                update();
                lastUpdateTime += timeBetweenUpdates;
                updateCount++;
            }

            // If for some reason an update takes forever, we don't want to do an insane number of catchups.
            // If you were doing some sort of game that needed to keep EXACT time, you would get rid of this.
            if (now - lastUpdateTime > timeBetweenUpdates) {
                lastUpdateTime = now;
            }

            // Render. To do so, we need to calculate interpolation for a smooth render.
            float interpolation = Math.min(1.0f, (float) (now - lastUpdateTime) / timeBetweenUpdates);
            render(interpolation);
            onPostRender();

            frameCount++;

            // Update the frames we got.
            int thisSecond = (int) (lastUpdateTime / 1000000000.0f);
            if (thisSecond > lastSecondTime) {
                setFps(frameCount);
                frameCount = 0;
                lastSecondTime = thisSecond;
            }

            // Yield until it has been at least the target time between renders. This saves the CPU from hogging.
            if (!isVSync()) {
                while (shouldWait(now, lastUpdateTime, lastFrameTime)) {
                    Thread.yield();

                    // This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
                    // You can remove this line, and it will still work (better), your CPU just climbs on certain OSes.
                    // FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a look at different peoples' solutions to this.
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    now = System.nanoTime();
                }

                lastFrameTime = now;
            }
        }

        clear();
    }

    @Override
    protected boolean shouldWait(long now, double lastUpdateTime, long lastFrameTime) {
        return now - lastUpdateTime < timeBetweenUpdates;
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

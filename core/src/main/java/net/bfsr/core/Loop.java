package net.bfsr.core;

public abstract class Loop {

    protected final double GAME_HERTZ = 60.0;
    protected final double TIME_BETWEEN_UPDATES = 1_000_000_000 / GAME_HERTZ;

    protected abstract void input();
    protected abstract void postInputUpdate();
    protected abstract void update(double delta);
    protected abstract void render(float interpolation);
    protected abstract boolean isRunning();
    protected abstract void setFps(int fps);
    protected abstract void last();
    protected abstract void clear();
    protected abstract boolean isVSync();

    protected boolean shouldWaitBeforeNextFrame(double now, double lastUpdateTime) {
        return now - lastUpdateTime < TIME_BETWEEN_UPDATES;
    }

    protected void loop() {
        final double delta = 1.0 / GAME_HERTZ;
        // At the very most we will update the game this many times before a new render.
        // If you're worried about visual hitches more than perfect timing, set this to 1.
        final int MAX_UPDATES_BEFORE_RENDER = 1;
        // We will need the last update time.
        double lastUpdateTime = System.nanoTime();

        // Simple way of finding FPS.
        int lastSecondTime = (int) (lastUpdateTime / 1000000000);
        int frameCount = 0;

        while (isRunning()) {
            double now = System.nanoTime();

            int updateCount = 0;

            // Do as many game updates as we need to, potentially playing catchup.
            while (now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER) {
                input();
                update(delta);
                lastUpdateTime += TIME_BETWEEN_UPDATES;
                updateCount++;
                postInputUpdate();
            }

            // If for some reason an update takes forever, we don't want to do an insane number of catchups.
            // If you were doing some sort of game that needed to keep EXACT time, you would get rid of this.
            if (lastUpdateTime - now > TIME_BETWEEN_UPDATES) {
                lastUpdateTime = now - TIME_BETWEEN_UPDATES;
            }

            // Render. To do so, we need to calculate interpolation for a smooth render.
            float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES));
            render(interpolation);
            frameCount++;

            // Update the frames we got.
            int thisSecond = (int) (lastUpdateTime / 1000000000);
            if (thisSecond > lastSecondTime) {
                setFps(frameCount);
                frameCount = 0;
                lastSecondTime = thisSecond;
            }

            // Yield until it has been at least the target time between renders. This saves the CPU from hogging.
            if (!isVSync()) {
                while (shouldWaitBeforeNextFrame(now, lastUpdateTime)) {
                    Thread.yield();

                    // This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
                    // You can remove this line and it will still work (better), your CPU just climbs on certain OSes.
                    // FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a look at different peoples' solutions to this.
//                    try {
//                        Thread.sleep(1);
//                    } catch (Exception e) {
//                    }

                    now = System.nanoTime();
                }
            }

            last();
        }

        clear();
    }
}

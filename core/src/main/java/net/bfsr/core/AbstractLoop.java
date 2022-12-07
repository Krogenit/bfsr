package net.bfsr.core;

public abstract class AbstractLoop {
    private boolean running;

    protected abstract void loop();

    protected void run() {
        running = true;
    }

    protected abstract boolean shouldWait(double now, double lastUpdateTime);

    protected abstract void update();

    protected abstract void render(float interpolation);

    protected abstract void onPostRender();

    protected abstract void setFps(int fps);

    protected boolean isVSync() {
        return false;
    }

    protected abstract int getUpdatesPerSecond();

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    protected abstract void clear();
}

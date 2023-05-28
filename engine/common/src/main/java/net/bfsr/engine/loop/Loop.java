package net.bfsr.engine.loop;

public interface Loop {
    void loop();
    void run();
    void update();
    void render(float interpolation);
    void onPostRender();
    void setFps(int fps);
    int getUpdatesPerSecond();
    boolean isRunning();
    void clear();
}
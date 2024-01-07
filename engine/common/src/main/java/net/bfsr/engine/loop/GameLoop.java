package net.bfsr.engine.loop;

public interface GameLoop {
    void run();
    void update(double time);
    void render(float interpolation);
    void setFps(int fps);
    boolean isRunning();
    void clear();
}
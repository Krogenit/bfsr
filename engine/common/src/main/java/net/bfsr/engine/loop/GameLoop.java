package net.bfsr.engine.loop;

public interface GameLoop {
    void run();
    void update(int frame, double time);
    void render(float interpolation);
    void setFps(int fps);
    boolean isRunning();
    void clear();
}
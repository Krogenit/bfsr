package ru.krogenit.bfsr.util;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * The timer class is used for calculating delta time and also FPS and UPS
 * calculation.
 *
 * @author Heiko Brumme
 */
public class Timer {

    /**
     * System time since last loop.
     */
    private double lastLoopTime;
    /**
     * Used for FPS and UPS calculation.
     */
    private float timeCount;
    /**
     * Frames per second.
     */
    private int fps;
    /**
     * Counter for the FPS calculation.
     */
    private int fpsCount;
    /**
     * Updates per second.
     */
    private int ups;
    /**
     * Counter for the UPS calculation.
     */
    private int upsCount;

    /**
     * Initializes the timer.
     */
    public void init() {
        lastLoopTime = getTime();
    }

    /**
     * Returns the time elapsed since <code>glfwInit()</code> in seconds.
     *
     * @return System time in seconds
     */
    public double getTime() {
        return glfwGetTime();
    }

    /**
     * Returns the time that have passed since the last loop.
     *
     * @return Delta time in seconds
     */
    public float getDelta() {
        double time = getTime();
        float delta = (float) (time - lastLoopTime);
        lastLoopTime = time;
        timeCount += delta;
        return delta;
    }

    /**
     * Updates the FPS counter.
     */
    public void updateFPS() {
        fpsCount++;
    }

    /**
     * Updates the UPS counter.
     */
    public void updateUPS() {
        upsCount++;
    }

    /**
     * Updates FPS and UPS if a whole second has passed.
     */
    public void update() {
        if (timeCount > 1f) {
            fps = fpsCount;
            fpsCount = 0;

            ups = upsCount;
            upsCount = 0;

            timeCount -= 1f;
        }
    }

    /**
     * Getter for the FPS.
     *
     * @return Frames per second
     */
    public int getFPS() {
        return fps > 0 ? fps : fpsCount;
    }

    /**
     * Getter for the UPS.
     *
     * @return Updates per second
     */
    public int getUPS() {
        return ups > 0 ? ups : upsCount;
    }

    /**
     * Getter for the last loop time.
     *
     * @return System time of the last loop
     */
    public double getLastLoopTime() {
        return lastLoopTime;
    }

}
package net.bfsr.client.util;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class FpsSync {
    private static final long NANOS_IN_SECOND = 1000L * 1000L * 1000L;

    private long nextFrameTime;

    /** for calculating the averages the previous sleep/yield times are stored */
    private final RunningAvg sleepDurations = new RunningAvg(10);
    private final RunningAvg yieldDurations = new RunningAvg(10);

    /**
     * An accurate sync method that will attempt to run at a constant frame rate.
     *
     * @param fps - the desired frame rate, in frames per second
     */
    public void sync(int fps) {
        try {
            // sleep until the average sleep time is greater than the time remaining till nextFrameTime
            for (long t0 = getTime(), t1; (nextFrameTime - t0) > sleepDurations.avg(); t0 = t1) {
                Thread.sleep(1);
                sleepDurations.add((t1 = getTime()) - t0); // update average sleep time
            }

            // slowly dampen sleep average if too high to avoid yielding too much
            sleepDurations.dampenForLowResTicker();

            // yield until the average yield time is greater than the time remaining till nextFrameTime
            for (long t0 = getTime(), t1; (nextFrameTime - t0) > yieldDurations.avg(); t0 = t1) {
                Thread.yield();
                yieldDurations.add((t1 = getTime()) - t0); // update average yield time
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // schedule next frame, drop frame(s) if already too late for next frame
        nextFrameTime = Math.max(nextFrameTime + NANOS_IN_SECOND / fps, getTime());
    }

    /**
     * This method will initialise the sync method by setting initial
     * values for sleepDurations/yieldDurations and nextFrameTime.
     */
    public void init() {
        sleepDurations.init(1000 * 1000);
        yieldDurations.init((int) (-(getTime() - getTime()) * 1.333));

        nextFrameTime = getTime();
    }

    /**
     * Get the system time in nano seconds
     *
     * @return will return the current time in nano's
     */
    private long getTime() {
        return (long) (glfwGetTime() * NANOS_IN_SECOND);
    }

    private static final class RunningAvg {
        private final long[] slots;
        private int offset;
        private long sum;

        private static final long DAMPEN_THRESHOLD = 10 * 1000L * 1000L; // 10ms
        private static final float DAMPEN_FACTOR = 0.9f; // don't change: 0.9f is exactly right!

        private RunningAvg(int slotCount) {
            slots = new long[slotCount];
        }

        public void init(long value) {
            while (offset < slots.length) {
                slots[offset++] = value;
            }

            sum = value * slots.length;
        }

        public void add(long value) {
            int index = offset++ % slots.length;
            sum = sum - slots[index] + value;
            slots[index] = value;
            offset %= slots.length;
        }

        public long avg() {
            return sum / slots.length;
        }

        public void dampenForLowResTicker() {
            if (avg() > DAMPEN_THRESHOLD) {
                for (int i = 0; i < slots.length; i++) {
                    slots[i] *= DAMPEN_FACTOR;
                }

                sum *= DAMPEN_FACTOR;
            }
        }
    }
}
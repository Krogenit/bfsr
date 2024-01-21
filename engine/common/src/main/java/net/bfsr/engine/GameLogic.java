package net.bfsr.engine;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.event.EventBusManager;
import net.bfsr.engine.profiler.Profiler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
public class GameLogic {
    @Getter
    @Setter
    private int updatesPerSecond = Engine.getUpdatesPerSecond();
    @Getter
    @Setter
    private float updateDeltaTime = Engine.getUpdateDeltaTime();
    @Getter
    @Setter
    private double timeBetweenUpdates = Engine.getTimeBetweenUpdates();
    @Getter
    private boolean isRunning;
    @Setter
    @Getter
    private boolean paused;
    @Getter
    protected final Profiler profiler = new Profiler();

    /**
     * Queue of Runnable which will execute in next game logic update step
     *
     * @see GameLogic#update(double)
     */
    private final Queue<Runnable> futureTasks = new ConcurrentLinkedQueue<>();

    @Getter
    protected final EventBusManager eventBus = new EventBusManager();

    public void init() {
        isRunning = true;
    }

    public void update(double time) {
        profiler.startSection("tasks");
        while (!futureTasks.isEmpty()) {
            futureTasks.poll().run();
        }
        profiler.endSection();
    }

    /**
     * Adds Runnable to queue which will execute in next game logic update step by main game thread.
     * This method is thread-safe.
     *
     * @param runnable the Runnable to execute
     */
    public void addFutureTask(Runnable runnable) {
        futureTasks.add(runnable);
    }

    public void stop() {
        isRunning = false;
    }

    public void clear() {}

    public int convertToTicks(int value) {
        return value * updatesPerSecond;
    }

    public int convertToTicks(float value) {
        return (int) (value * updatesPerSecond);
    }

    public float convertToDeltaTime(float value) {
        return value * updateDeltaTime;
    }
}
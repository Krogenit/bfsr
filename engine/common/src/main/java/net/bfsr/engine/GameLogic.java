package net.bfsr.engine;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.profiler.Profiler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameLogic {
    @Getter
    @Setter
    private boolean isRunning;
    @Getter
    protected final Profiler profiler = new Profiler();

    private final Queue<Runnable> futureTasks = new ConcurrentLinkedQueue<>();

    public void init() {
        isRunning = true;
    }

    public void update() {
        profiler.startSection("tasks");
        while (!futureTasks.isEmpty()) {
            futureTasks.poll().run();
        }
    }

    public void addFutureTask(Runnable runnable) {
        futureTasks.add(runnable);
    }

    public void stop() {
        isRunning = false;
    }
}
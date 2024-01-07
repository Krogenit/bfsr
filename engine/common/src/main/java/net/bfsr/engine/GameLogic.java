package net.bfsr.engine;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.profiler.Profiler;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
public class GameLogic {
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
    protected final EventBus eventBus = createEventBus();

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

    public void publish(Event event) {
        eventBus.publish(event);
    }

    public void subscribe(Object eventListener) {
        eventBus.subscribe(eventListener);
    }

    private EventBus createEventBus() {
        IBusConfiguration config = new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default())
                .setProperty(IBusConfiguration.Properties.BusId, "Main Event Bus")
                .addPublicationErrorHandler(error -> log.error(error.getMessage(), error.getCause()));

        return new EventBus(config);
    }

    public void stop() {
        isRunning = false;
    }

    public void clear() {}
}
package net.bfsr.engine.event;

public class OneTimeEventListener<T extends Event> implements EventListener<T> {
    private final EventBus<T> eventBus;
    private final EventListener<T> eventListener;

    public OneTimeEventListener(EventBus<T> eventBus, EventListener<T> eventListener) {
        this.eventBus = eventBus;
        this.eventListener = eventListener;
    }

    @Override
    public void run(T event) {
        eventListener.run(event);
        eventBus.removeListener(this);
    }
}
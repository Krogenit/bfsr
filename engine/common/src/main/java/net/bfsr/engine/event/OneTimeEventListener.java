package net.bfsr.engine.event;

public class OneTimeEventListener<T extends Event> implements EventListener<T> {
    private final Listeners<T> listeners;
    private final EventListener<T> eventListener;

    public OneTimeEventListener(Listeners<T> listeners, EventListener<T> eventListener) {
        this.listeners = listeners;
        this.eventListener = eventListener;
    }

    @Override
    public void run(T event) {
        eventListener.run(event);
        listeners.removeListener(this);
    }
}
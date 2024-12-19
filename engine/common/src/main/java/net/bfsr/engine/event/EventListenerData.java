package net.bfsr.engine.event;

import java.util.function.Function;

final class EventListenerData {
    private final Listeners<Event> listeners;
    private final Function<Object, EventListener<Event>> eventListenerFunction;
    private EventListener<Event> eventListenerMethod;

    EventListenerData(Listeners<Event> listeners, Function<Object, EventListener<Event>> eventListenerMethodFunction) {
        this.listeners = listeners;
        this.eventListenerFunction = eventListenerMethodFunction;
    }

    void addListener(Object listener) {
        eventListenerMethod = eventListenerFunction.apply(listener);
        listeners.addListener(eventListenerMethod);
    }

    void removeEventListener() {
        listeners.removeListener(eventListenerMethod);
    }
}
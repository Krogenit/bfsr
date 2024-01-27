package net.bfsr.engine.event;

@SuppressWarnings("rawtypes")
record EventListenerData(Listeners<? extends Event> listeners, EventListener eventListener) {
    void addListener() {
        listeners.addListener(eventListener);
    }

    void removeEventListener() {
        listeners.removeListener(eventListener);
    }
}
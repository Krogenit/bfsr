package net.bfsr.engine.event;

@SuppressWarnings("rawtypes")
record EventListenerData(EventBus<? extends Event> eventBus, EventListener eventListener) {
    void addListener() {
        eventBus.addListener(eventListener);
    }

    void removeEventListener() {
        eventBus.removeListener(eventListener);
    }
}
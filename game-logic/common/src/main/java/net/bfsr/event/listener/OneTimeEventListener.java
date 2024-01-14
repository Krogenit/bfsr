package net.bfsr.event.listener;

import net.bfsr.event.EventBus;

public class OneTimeEventListener implements EventListener {
    private final EventBus eventBus;
    private final Runnable onEventRunnable;

    public OneTimeEventListener(EventBus eventBus, Runnable onEventRunnable) {
        this.eventBus = eventBus;
        this.onEventRunnable = onEventRunnable;

    }

    @Override
    public void event() {
        onEventRunnable.run();
        eventBus.removeListener(this);
    }
}
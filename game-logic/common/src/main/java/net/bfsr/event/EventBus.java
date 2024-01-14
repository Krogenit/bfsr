package net.bfsr.event;

import net.bfsr.event.listener.EventListener;

public interface EventBus {
    void removeListener(EventListener listener);
}
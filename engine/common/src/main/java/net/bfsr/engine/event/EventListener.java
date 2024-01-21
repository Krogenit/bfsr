package net.bfsr.engine.event;

@FunctionalInterface
public interface EventListener<T extends Event> {
    void run(T event);
}
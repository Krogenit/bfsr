package net.bfsr.engine.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final Deque<T> particles = new ArrayDeque<>();

    public T getOrCreate(Supplier<T> supplier) {
        if (particles.isEmpty()) {
            return supplier.get();
        } else {
            return particles.pollFirst();
        }
    }

    public void returnBack(T particle) {
        particles.addFirst(particle);
    }
}
package net.bfsr.engine.util;

import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ObjectPool<T> {
    private final Deque<T> objects = new ArrayDeque<>();
    private final Supplier<T> supplier;

    public T get() {
        if (objects.isEmpty()) {
            return supplier.get();
        } else {
            return objects.pollFirst();
        }
    }

    public void returnBack(T particle) {
        objects.addFirst(particle);
    }

    public int size() {
        return objects.size();
    }

    public void clear() {
        objects.clear();
    }
}
package net.bfsr.engine.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final Deque<T> objects = new ArrayDeque<>();
    private final Supplier<T> supplier;

    public ObjectPool(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public ObjectPool(Function<ObjectPool<T>, T> function) {
        this.supplier = () -> function.apply(this);
    }

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
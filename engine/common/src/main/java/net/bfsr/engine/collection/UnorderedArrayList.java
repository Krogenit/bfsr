package net.bfsr.engine.collection;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.lang.reflect.Array;
import java.util.Arrays;

@Getter
@Accessors(fluent = true)
public class UnorderedArrayList<T> {
    private T[] items;
    private int size;

    @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
    public UnorderedArrayList(int capacity) {
        this.items = (T[]) new Object[capacity];
    }

    public UnorderedArrayList() {
        this(16);
    }

    public UnorderedArrayList(UnorderedArrayList<T> unorderedArrayList) {
        this(unorderedArrayList.size);
        this.size = unorderedArrayList.size;
        System.arraycopy(unorderedArrayList.items, 0, this.items, 0, this.size);
    }

    public void add(T value) {
        if (size == items.length) {
            items = resize(Math.max(8, (int) (size * 1.75F)));
        }

        items[size++] = value;
    }

    private T[] resize(int newSize) {
        T[] newItems = (T[]) Array.newInstance(items.getClass().getComponentType(), newSize);
        System.arraycopy(items, 0, newItems, 0, Math.min(size, newItems.length));
        items = newItems;
        return newItems;
    }

    public T get(int index) {
        return items[index];
    }

    public void set(int index, T value) {
        items[index] = value;
    }

    public T remove(int index) {
        T value = items[index];
        --size;
        items[index] = items[size];
        items[size] = null;
        return value;
    }

    public boolean remove(T value) {
        for (int i = 0; i < size; ++i) {
            if (items[i] == value) {
                remove(i);
                return true;
            }
        }

        return false;
    }

    public void clear() {
        Arrays.fill(items, 0, size, null);
        size = 0;
    }
}
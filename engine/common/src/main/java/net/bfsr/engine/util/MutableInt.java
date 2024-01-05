package net.bfsr.engine.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class MutableInt {
    private int value;

    public void set(int value) {
        this.value = value;
    }

    public int getAndIncrement() {
        int oldValue = value;
        value++;
        return oldValue;
    }

    public int getAndAdd(int i) {
        int oldValue = value;
        value += i;
        return oldValue;
    }

    public void add(int value) {
        this.value += value;
    }

    public int get() {
        return value;
    }
}
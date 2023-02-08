package net.bfsr.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class MutableInt {
    private int value;

    public void increment() {
        value++;
    }

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

    public int get() {
        return value;
    }
}

package net.bfsr.util;

public class MutableInt {
    private int value;

    public void set(int value) {
        this.value = value;
    }

    public int getAndAdd(int i) {
        int oldValue = value;
        value += i;
        return oldValue;
    }
}

package net.bfsr.component.cargo;

public class Cargo {
    private int capacity;
    private final int maxCapacity;

    public Cargo(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public boolean addToCargo(int value) {
        if (capacity + value > maxCapacity) return false;

        capacity += value;
        return true;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}

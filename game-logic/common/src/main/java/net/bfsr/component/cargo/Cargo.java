package net.bfsr.component.cargo;

import lombok.Getter;
import net.bfsr.config.component.cargo.CargoData;

@Getter
public class Cargo {
    private int capacity;
    private final int maxCapacity;

    public Cargo(CargoData cargoData) {
        this.maxCapacity = cargoData.getMaxCapacity();
    }

    public boolean addToCargo(int value) {
        if (capacity + value > maxCapacity) return false;

        capacity += value;
        return true;
    }
}
package net.bfsr.entity.ship.module.cargo;

import lombok.Getter;
import net.bfsr.config.component.cargo.CargoData;
import net.bfsr.entity.ship.module.Module;
import net.bfsr.entity.ship.module.ModuleType;

@Getter
public class Cargo extends Module {
    private final CargoData data;
    private int capacity;
    private final int maxCapacity;

    public Cargo(CargoData cargoData) {
        this.data = cargoData;
        this.maxCapacity = cargoData.getMaxCapacity();
    }

    public boolean addToCargo(int value) {
        if (capacity + value > maxCapacity) return false;

        capacity += value;
        return true;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.CARGO;
    }
}
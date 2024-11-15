package net.bfsr.config.component.cargo;

import lombok.Getter;
import net.bfsr.config.ConfigData;

@Getter
public class CargoData extends ConfigData {
    private final int maxCapacity;

    CargoData(CargoConfig cargoConfig, String fileName, int id, int registryId) {
        super(fileName, id, registryId);
        this.maxCapacity = cargoConfig.maxCapacity();
    }
}
package net.bfsr.config.component.cargo;

import lombok.Getter;
import net.bfsr.config.ConfigData;

@Getter
public class CargoData extends ConfigData {
    private final int maxCapacity;

    public CargoData(CargoConfig cargoConfig, int dataIndex) {
        super(cargoConfig.name(), dataIndex);
        this.maxCapacity = cargoConfig.maxCapacity();
    }
}
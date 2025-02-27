package net.bfsr.config.component.cargo;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class CargoRegistry extends ConfigToDataConverter<CargoConfig, CargoData> {
    public CargoRegistry() {
        super("module/cargo", CargoConfig.class, (fileName, cargoConfig) -> fileName, CargoData::new);
    }
}
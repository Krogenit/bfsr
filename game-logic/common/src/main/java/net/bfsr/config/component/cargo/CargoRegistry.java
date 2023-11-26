package net.bfsr.config.component.cargo;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class CargoRegistry extends ConfigToDataConverter<CargoConfig, CargoData> {
    public static final CargoRegistry INSTANCE = new CargoRegistry();

    private CargoRegistry() {
        super("module/cargo", CargoConfig.class, (fileName, cargoConfig) -> fileName, CargoData::new);
    }
}
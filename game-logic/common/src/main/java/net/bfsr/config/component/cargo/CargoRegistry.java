package net.bfsr.config.component.cargo;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class CargoRegistry extends ConfigToDataConverter<CargoConfig, CargoData> {
    public static final CargoRegistry INSTANCE = new CargoRegistry();

    public CargoRegistry() {
        super("module/cargo", CargoConfig.class, CargoConfig::name, CargoData::new);
    }
}
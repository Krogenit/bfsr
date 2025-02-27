package net.bfsr.config.component.armor;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class ArmorPlateRegistry extends ConfigToDataConverter<ArmorPlateConfig, ArmorPlateData> {
    public ArmorPlateRegistry() {
        super("module/armor", ArmorPlateConfig.class, (fileName, armorPlateConfig) -> fileName, ArmorPlateData::new);
    }
}
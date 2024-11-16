package net.bfsr.config.component.armor;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class ArmorPlateRegistry extends ConfigToDataConverter<ArmorPlateConfig, ArmorPlateData> {
    public ArmorPlateRegistry() {
        super("module/armor", ArmorPlateConfig.class, (fileName, armorPlateConfig) -> fileName, ArmorPlateData::new);
    }
}
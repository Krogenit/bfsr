package net.bfsr.config.component.armor;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class ArmorPlateRegistry extends ConfigToDataConverter<ArmorPlateConfig, ArmorPlateData> {
    public static final ArmorPlateRegistry INSTANCE = new ArmorPlateRegistry();

    public ArmorPlateRegistry() {
        super("module/armor", ArmorPlateConfig.class, ArmorPlateConfig::getName, ArmorPlateData::new);
    }
}
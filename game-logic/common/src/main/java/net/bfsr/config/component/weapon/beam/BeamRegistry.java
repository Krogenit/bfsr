package net.bfsr.config.component.weapon.beam;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class BeamRegistry extends ConfigToDataConverter<BeamConfig, BeamData> {
    public static final BeamRegistry INSTANCE = new BeamRegistry();

    public BeamRegistry() {
        super("module/weapon/beam", BeamConfig.class, BeamConfig::name, BeamData::new);
    }
}
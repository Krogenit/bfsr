package net.bfsr.config.component.weapon.beam;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class BeamRegistry extends ConfigToDataConverter<BeamConfig, BeamData> {
    public static final BeamRegistry INSTANCE = new BeamRegistry();

    private BeamRegistry() {
        super("module/weapon/beam", BeamConfig.class, (fileName, beamConfig) -> fileName, BeamData::new);
    }
}
package net.bfsr.config.weapon.beam;

import net.bfsr.config.ConfigRegistry;

public class BeamRegistry extends ConfigRegistry<BeamConfig, BeamData> {
    public static final BeamRegistry INSTANCE = new BeamRegistry();

    public BeamRegistry() {
        super("weapon/beam", BeamConfig.class, BeamConfig::name, BeamData::new);
    }
}
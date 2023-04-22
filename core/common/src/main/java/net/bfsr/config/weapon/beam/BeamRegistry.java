package net.bfsr.config.weapon.beam;

import net.bfsr.config.ConfigRegistry;
import net.bfsr.util.PathHelper;

public class BeamRegistry extends ConfigRegistry<BeamConfig, BeamData> {
    public static final BeamRegistry INSTANCE = new BeamRegistry();

    public BeamRegistry() {
        super(PathHelper.CONFIG.resolve("weapon/beam"), BeamConfig.class, BeamConfig::name, BeamData::new);
    }
}
package net.bfsr.config.weapon.gun;

import net.bfsr.config.ConfigRegistry;
import net.bfsr.util.PathHelper;

public class GunRegistry extends ConfigRegistry<GunConfig, GunData> {
    public static final GunRegistry INSTANCE = new GunRegistry();

    public GunRegistry() {
        super(PathHelper.CONFIG.resolve("weapon/gun"), GunConfig.class, GunConfig::name, GunData::new);
    }
}
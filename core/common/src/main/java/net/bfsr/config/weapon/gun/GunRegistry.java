package net.bfsr.config.weapon.gun;

import net.bfsr.config.ConfigRegistry;

public class GunRegistry extends ConfigRegistry<GunConfig, GunData> {
    public static final GunRegistry INSTANCE = new GunRegistry();

    public GunRegistry() {
        super("weapon/gun", GunConfig.class, GunConfig::name, GunData::new);
    }
}
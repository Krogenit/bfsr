package net.bfsr.config.component.weapon.gun;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class GunRegistry extends ConfigToDataConverter<GunConfig, GunData> {
    public static final GunRegistry INSTANCE = new GunRegistry();

    public GunRegistry() {
        super("module/weapon/gun", GunConfig.class, GunConfig::name, GunData::new);
    }
}
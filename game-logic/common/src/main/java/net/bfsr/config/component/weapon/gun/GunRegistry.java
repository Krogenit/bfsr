package net.bfsr.config.component.weapon.gun;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class GunRegistry extends ConfigToDataConverter<GunConfig, GunData> {
    public GunRegistry() {
        super("module/weapon/gun", GunConfig.class, (fileName, gunConfig) -> fileName, GunData::new);
    }
}
package net.bfsr.config.component.weapon.gun;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class GunRegistry extends ConfigToDataConverter<GunConfig, GunData> {
    public GunRegistry() {
        super("module/weapon/gun", GunConfig.class, (fileName, gunConfig) -> fileName, GunData::new);
    }
}
package net.bfsr.config.entity.bullet;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class BulletRegistry extends ConfigToDataConverter<BulletConfig, BulletData> {
    public static final BulletRegistry INSTANCE = new BulletRegistry();

    public BulletRegistry() {
        super("entity/bullet", BulletConfig.class, BulletConfig::name, BulletData::new);
    }
}
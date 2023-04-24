package net.bfsr.config.bullet;

import net.bfsr.config.ConfigRegistry;

public class BulletRegistry extends ConfigRegistry<BulletConfig, BulletData> {
    public static final BulletRegistry INSTANCE = new BulletRegistry();

    public BulletRegistry() {
        super("bullet", BulletConfig.class, BulletConfig::name, BulletData::new);
    }
}
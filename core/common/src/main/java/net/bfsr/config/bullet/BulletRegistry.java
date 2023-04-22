package net.bfsr.config.bullet;

import net.bfsr.config.ConfigRegistry;
import net.bfsr.util.PathHelper;

public class BulletRegistry extends ConfigRegistry<BulletConfig, BulletData> {
    public static final BulletRegistry INSTANCE = new BulletRegistry();

    public BulletRegistry() {
        super(PathHelper.CONFIG.resolve("bullet"), BulletConfig.class, BulletConfig::name, BulletData::new);
    }
}
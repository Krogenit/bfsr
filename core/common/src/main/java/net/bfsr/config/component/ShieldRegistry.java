package net.bfsr.config.component;

import net.bfsr.config.ConfigRegistry;

public class ShieldRegistry extends ConfigRegistry<ShieldConfig, ShieldData> {
    public static final ShieldRegistry INSTANCE = new ShieldRegistry();

    public ShieldRegistry() {
        super("shield", ShieldConfig.class, ShieldConfig::name, ShieldData::new);
    }
}
package net.bfsr.component.shield;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.bfsr.config.ConfigLoader;
import net.bfsr.config.component.ShieldConfig;

import java.nio.file.Path;

public class ShieldRegistry {
    public static final ShieldRegistry INSTANCE = new ShieldRegistry();

    private final TMap<String, ShieldConfig> registeredShield = new THashMap<>();

    public void init(Path file) {
        ConfigLoader.loadFromFiles(file.resolve("shield"), ShieldConfig.class, shieldConfig -> registeredShield.put(shieldConfig.getName(), shieldConfig));
    }

    public ShieldConfig getShield(String name) {
        return registeredShield.get(name);
    }
}
package net.bfsr.component.shield;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.bfsr.config.ConfigLoader;
import net.bfsr.config.component.ShieldConfig;

import java.io.File;

public class ShieldRegistry {
    public static final ShieldRegistry INSTANCE = new ShieldRegistry();

    private final TMap<String, ShieldConfig> registeredShield = new THashMap<>();

    public void init(File file) {
        ConfigLoader.loadFromFiles(new File(file, "shield"), ShieldConfig.class, shieldConfig -> registeredShield.put(shieldConfig.getName(), shieldConfig));
    }

    public ShieldConfig getShield(String name) {
        return registeredShield.get(name);
    }
}

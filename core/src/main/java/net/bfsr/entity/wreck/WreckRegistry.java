package net.bfsr.entity.wreck;

import net.bfsr.config.ConfigLoader;
import net.bfsr.entity.ship.ShipType;
import net.bfsr.util.PathHelper;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public class WreckRegistry {
    public static final WreckRegistry INSTANCE = new WreckRegistry();

    private final Map<ShipType, RegisteredShipWreck[]> map = new EnumMap<>(ShipType.class);

    public void init() {
        ConfigLoader.loadFromFiles(new File(PathHelper.CONFIG, "wreck"), file -> {
            WrecksConfig wrecksConfig = ConfigLoader.load(file, WrecksConfig.class);
            WreckConfig[] wrecks = wrecksConfig.getWrecks();
            RegisteredShipWreck[] registeredShipWrecks = new RegisteredShipWreck[wrecks.length];
            for (int i = 0; i < registeredShipWrecks.length; i++) {
                registeredShipWrecks[i] = new RegisteredShipWreck(wrecks[i].getTexturePath(), wrecks[i].getFireTexturePath(), wrecks[i].getSparkleTexturePath(), wrecks[i].getVertices());
            }
            map.put(wrecksConfig.getShipType(), registeredShipWrecks);
        });
    }

    public RegisteredShipWreck[] getWrecks(ShipType shipType) {
        return map.get(shipType);
    }
}

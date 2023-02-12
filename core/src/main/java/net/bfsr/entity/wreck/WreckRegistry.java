package net.bfsr.entity.wreck;

import net.bfsr.config.ConfigLoader;
import net.bfsr.entity.ship.ShipType;
import net.bfsr.util.PathHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class WreckRegistry {
    public static final WreckRegistry INSTANCE = new WreckRegistry();

    private final Map<ShipType, RegisteredShipWreck[]> wrecksByShipType = new EnumMap<>(ShipType.class);
    private final List<RegisteredShipWreck>[] wrecksByType = new List[2];

    public void init() {
        wrecksByType[WreckType.SMALL.ordinal()] = new ArrayList<>();
        wrecksByType[WreckType.DEFAULT.ordinal()] = new ArrayList<>();
        ConfigLoader.loadFromFiles(new File(PathHelper.CONFIG, "wreck"), file -> {
            WrecksConfig wrecksConfig = ConfigLoader.load(file, WrecksConfig.class);
            WreckConfig[] wrecks = wrecksConfig.getWrecks();
            if (wrecksConfig.getShipType() != null) {
                RegisteredShipWreck[] registeredShipWrecks = new RegisteredShipWreck[wrecks.length];
                for (int i = 0; i < registeredShipWrecks.length; i++) {
                    WreckConfig wreck = wrecks[i];
                    registeredShipWrecks[i] = new RegisteredShipWreck(wreck.getTexturePath(), wreck.getFireTexturePath(), wreck.getSparkleTexturePath(), wreck.getVertices());
                }
                wrecksByShipType.put(wrecksConfig.getShipType(), registeredShipWrecks);
            } else {
                for (int i = 0; i < wrecks.length; i++) {
                    WreckConfig wreck = wrecks[i];
                    wrecksByType[wreck.getType().ordinal()].add(new RegisteredShipWreck(wreck.getTexturePath(), wreck.getFireTexturePath(), wreck.getSparkleTexturePath(), wreck.getVertices()));
                }
            }
        });
    }

    public RegisteredShipWreck[] getWrecks(ShipType shipType) {
        return wrecksByShipType.get(shipType);
    }

    public RegisteredShipWreck getWreck(WreckType wreckType, int index) {
        return wrecksByType[wreckType.ordinal()].get(index);
    }
}

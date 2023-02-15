package net.bfsr.entity.wreck;

import lombok.Getter;
import net.bfsr.config.Configurable;
import net.bfsr.entity.ship.ShipType;

@Configurable
@Getter
public class WrecksConfig {
    private ShipType shipType;
    private WreckConfig[] wrecks;
}

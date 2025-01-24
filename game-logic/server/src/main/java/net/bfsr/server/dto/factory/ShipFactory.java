package net.bfsr.server.dto.factory;

import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.entity.ship.Ship;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.dto.ShipModel;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;

public class ShipFactory {
    private final ShipRegistry shipRegistry = ServerGameLogic.get().getConfigConverterManager().getConverter(ShipRegistry.class);

    @ObjectFactory
    public <T extends Ship> T createEntity(ShipModel shipModel, @TargetType Class<T> entityClass) {
        return (T) new Ship(shipRegistry.get(shipModel.name()), new DamageMask(32, 32));
    }
}

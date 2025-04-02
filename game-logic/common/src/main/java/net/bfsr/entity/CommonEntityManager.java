package net.bfsr.entity;

import net.bfsr.engine.world.entity.AbstractEntityManager;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;

public class CommonEntityManager extends AbstractEntityManager {
    @Override
    public void registerEntities() {
        registerEntity(Ship.class);
        registerEntity(Bullet.class);
        registerEntity(ShipWreck.class);
        registerEntity(Wreck.class);
    }
}

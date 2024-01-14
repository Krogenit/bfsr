package net.bfsr.editor.object.ship;

import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.damage.DamageMask;
import net.bfsr.entity.ship.Ship;

public class TestShip extends Ship {
    public TestShip(ShipData shipData) {
        super(shipData, new DamageMask(32, 32, null));
    }

    @Override
    public void setSpawned() {
        spawned = true;
    }

    @Override
    public void setDead() {
        isDead = true;
    }
}
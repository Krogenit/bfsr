package net.bfsr.client.listener.entity;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.ship.ShipDestroyEvent;
import net.bfsr.event.entity.ship.ShipDestroyingExplosionEvent;

public class ShipEventListener {
    private final XoRoShiRo128PlusRandom rand = new XoRoShiRo128PlusRandom();

    @EventHandler
    public EventListener<ShipDestroyEvent> shipDestroyEvent() {
        return event -> ExplosionEffects.spawnDestroyShipSmall(event.ship());
    }

    @EventHandler
    public EventListener<ShipDestroyingExplosionEvent> shipDestroyingExplosionEvent() {
        return event -> {
            Ship ship = event.ship();
            float sizeX = ship.getSizeX();
            float sizeY = ship.getSizeY();
            float randomVectorX = -sizeX * 0.4f + sizeX * 0.8f * rand.nextFloat();
            float randomVectorY = -sizeY * 0.4f + sizeY * 0.8f * rand.nextFloat();
            ExplosionEffects.spawnSmallExplosion(ship.getX() + randomVectorX, ship.getY() + randomVectorY, 2.0f);
        };
    }
}
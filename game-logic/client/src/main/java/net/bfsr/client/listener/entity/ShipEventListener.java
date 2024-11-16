package net.bfsr.client.listener.entity;

import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.ship.ShipDestroyEvent;
import net.bfsr.event.entity.ship.ShipDestroyingExplosionEvent;
import net.bfsr.world.World;

import java.util.Random;

public class ShipEventListener {
    @EventHandler
    public EventListener<ShipDestroyEvent> shipDestroyEvent() {
        return event -> ExplosionEffects.spawnDestroyShipSmall(event.ship());
    }

    @EventHandler
    public EventListener<ShipDestroyingExplosionEvent> shipDestroyingExplosionEvent() {
        return event -> {
            Ship ship = event.ship();
            World world = ship.getWorld();
            float sizeX = ship.getSizeX();
            float sizeY = ship.getSizeY();
            Random rand = world.getRand();
            float randomVectorX = -sizeX * 0.4f + sizeX * 0.8f * rand.nextFloat();
            float randomVectorY = -sizeY * 0.4f + sizeY * 0.8f * rand.nextFloat();
            ExplosionEffects.spawnSmallExplosion(ship.getX() + randomVectorX, ship.getY() + randomVectorY, 2.0f);
        };
    }
}
package net.bfsr.client.listener.entity;

import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.ship.ShipDestroyEvent;
import net.bfsr.event.entity.ship.ShipDestroyingExplosionEvent;
import net.bfsr.world.World;
import org.joml.Vector2f;

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
            Vector2f position = ship.getPosition();
            Vector2f size = ship.getSize();
            Random rand = world.getRand();
            float randomVectorX = -size.x * 0.4f + size.x * 0.8f * rand.nextFloat();
            float randomVectorY = -size.y * 0.4f + size.y * 0.8f * rand.nextFloat();
            ExplosionEffects.spawnSmallExplosion(position.x + randomVectorX, position.y + randomVectorY, 2.0f);
        };
    }
}
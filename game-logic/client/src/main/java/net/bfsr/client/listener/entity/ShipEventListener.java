package net.bfsr.client.listener.entity;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.client.Client;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.renderer.entity.Render;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.ship.ShipDestroyEvent;
import net.bfsr.event.entity.ship.ShipDestroyingExplosionEvent;
import org.jbox2d.common.Vector2;

public class ShipEventListener {
    private final XoRoShiRo128PlusRandom rand = new XoRoShiRo128PlusRandom();
    private final ExplosionEffects explosionEffects = Client.get().getParticleEffects().getExplosionEffects();

    @EventHandler
    public EventListener<ShipDestroyEvent> shipDestroyEvent() {
        return event -> explosionEffects.spawnDestroyShipSmall(event.ship());
    }

    @EventHandler
    public EventListener<ShipDestroyingExplosionEvent> shipDestroyingExplosionEvent() {
        return event -> {
            Ship ship = event.ship();
            float sizeX = ship.getSizeX();
            float sizeY = ship.getSizeY();
            float randomVectorX = -sizeX * 0.4f + sizeX * 0.8f * rand.nextFloat();
            float randomVectorY = -sizeY * 0.4f + sizeY * 0.8f * rand.nextFloat();
            Render render = Client.get().getEntityRenderer().getRender(ship.getId());
            Vector2 linearVelocity = ship.getLinearVelocity();
            explosionEffects.spawnSmallExplosion(ship.getX() + randomVectorX, ship.getY() + randomVectorY, render.getZ(), 0.2f,
                    linearVelocity.x, linearVelocity.y);
        };
    }
}
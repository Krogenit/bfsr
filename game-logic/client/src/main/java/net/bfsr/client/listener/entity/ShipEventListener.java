package net.bfsr.client.listener.entity;

import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.event.entity.ship.ShipCollisionWithWreckEvent;
import net.bfsr.event.entity.ship.ShipDestroyEvent;
import net.bfsr.event.entity.ship.ShipDestroyingExplosionEvent;
import net.bfsr.event.entity.ship.ShipHullDamageByCollisionEvent;
import net.bfsr.math.RotationHelper;
import net.bfsr.world.World;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class ShipEventListener {
    private final Vector2f angleToVelocity = new Vector2f();

    @EventHandler
    public EventListener<ShipCollisionWithWreckEvent> shipCollisionWithWreckEvent() {
        return event -> {
            Ship ship = event.ship();
            Shield shield = ship.getModules().getShield();
            if (shield != null) {
                Vector4f color = ship.getConfigData().getEffectsColor();
                WeaponEffects.spawnDirectedSpark(event.contactX(), event.contactY(), event.normalX(), event.normalY(), 4.5f,
                        color.x, color.y, color.z, color.w);
            } else {
                WeaponEffects.spawnDirectedSpark(event.contactX(), event.contactY(), event.normalX(), event.normalY(), 3.75f,
                        1.0f, 1.0f, 1.0f, 1.0f);
            }
        };
    }

    @EventHandler
    public EventListener<ShipHullDamageByCollisionEvent> shipHullDamageByCollisionEvent() {
        return event -> {
            Ship ship = event.ship();
            World world = ship.getWorld();
            Random rand = world.getRand();
            Vector2f velocity = ship.getVelocity();
            WeaponEffects.spawnDirectedSpark(event.contactX(), event.contactY(), event.normalX(), event.normalY(), 3.75f, 1.0f,
                    1.0f, 1.0f, 1.0f);
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.15f, angleToVelocity);
            GarbageSpawner.smallGarbage(rand.nextInt(4), event.contactX(), event.contactY(),
                    velocity.x * 0.25f + angleToVelocity.x, velocity.y * 0.25f + angleToVelocity.y, 2.0f * rand.nextFloat());
        };
    }

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
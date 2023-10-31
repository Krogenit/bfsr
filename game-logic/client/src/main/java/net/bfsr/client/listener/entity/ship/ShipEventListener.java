package net.bfsr.client.listener.entity.ship;

import net.bfsr.client.Core;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.JumpEffects;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.event.entity.ship.*;
import net.bfsr.math.RotationHelper;
import net.bfsr.world.World;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

import static net.bfsr.math.RigidBodyUtils.ANGLE_TO_VELOCITY;

@Listener(references = References.Strong)
public class ShipEventListener {
    private final RenderManager renderManager = Core.get().getRenderManager();

    @Handler
    public void event(ShipAddToWorldEvent event) {
        renderManager.addRender(new ShipRender(event.ship()));
    }

    @Handler
    public void event(ShipCollisionWithWreckEvent event) {
        Ship ship = event.ship();
        Shield shield = ship.getModules().getShield();
        if (shield != null) {
            Vector4f color = ship.getShipData().getEffectsColor();
            WeaponEffects.spawnDirectedSpark(event.contactX(), event.contactY(), event.normalX(), event.normalY(), 4.5f,
                    color.x, color.y, color.z, color.w);
        } else {
            WeaponEffects.spawnDirectedSpark(event.contactX(), event.contactY(), event.normalX(), event.normalY(), 3.75f,
                    1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @Handler
    public void event(ShipSpawnEvent event) {
        Ship ship = event.ship();
        Vector2f velocity = ship.getVelocity();
        Vector2f position = ship.getPosition();
        Vector2f size = ship.getSize();
        Vector4f effectsColor = ship.getShipData().getEffectsColor();
        JumpEffects.jump(position.x, position.y, 32.0f + size.x * 0.25f, velocity.x * 0.5f, velocity.y * 0.5f,
                effectsColor.x, effectsColor.y, effectsColor.z, 1.0f);
        ShipRender render = renderManager.getRender(ship.getId());
        render.createName();
    }

    @Handler
    public void event(ShipHullDamageByCollisionEvent event) {
        Ship ship = event.ship();
        World world = ship.getWorld();
        Random rand = world.getRand();
        Vector2f velocity = ship.getVelocity();
        WeaponEffects.spawnDirectedSpark(event.contactX(), event.contactY(), event.normalX(), event.normalY(), 3.75f, 1.0f, 1.0f, 1.0f, 1.0f);
        RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.15f, ANGLE_TO_VELOCITY);
        GarbageSpawner.smallGarbage(rand.nextInt(4), event.contactX(), event.contactY(),
                velocity.x * 0.25f + ANGLE_TO_VELOCITY.x, velocity.y * 0.25f + ANGLE_TO_VELOCITY.y, 2.0f * rand.nextFloat());
    }

    @Handler
    public void event(ShipDestroyingExplosionEvent event) {
        Ship ship = event.ship();
        World world = ship.getWorld();
        Vector2f position = ship.getPosition();
        Vector2f size = ship.getSize();
        Random rand = world.getRand();
        float randomVectorX = -size.x * 0.4f + size.x * 0.8f * rand.nextFloat();
        float randomVectorY = -size.y * 0.4f + size.y * 0.8f * rand.nextFloat();
        ExplosionEffects.spawnSmallExplosion(position.x + randomVectorX, position.y + randomVectorY, 2.0f);
    }

    @Handler
    public void event(ShipDestroyEvent event) {
        ExplosionEffects.spawnDestroyShipSmall(event.ship());
    }
}
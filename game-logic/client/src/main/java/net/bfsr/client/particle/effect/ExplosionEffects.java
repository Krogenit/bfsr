package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.entity.RigidBody;
import org.jbox2d.common.Vector2;

public final class ExplosionEffects {
    private static final ParticleEffect shipDestroySmall = ParticleEffectsRegistry.INSTANCE.get("explosion/ship_small");
    private static final ParticleEffect smallExplosion = ParticleEffectsRegistry.INSTANCE.get("explosion/small");

    public static void spawnDestroyShipSmall(RigidBody ship) {
        Vector2 velocity = ship.getLinearVelocity();
        shipDestroySmall.play(ship.getX(), ship.getY(), ship.getSizeX(), ship.getSizeY(), velocity.x, velocity.y);
    }

    public static void spawnSmallExplosion(float x, float y, float size) {
        smallExplosion.play(x, y, size, size);
    }
}
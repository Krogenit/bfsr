package net.bfsr.client.particle.effect;

import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.particle.ParticleEffect;
import net.bfsr.client.particle.ParticleEffectsRegistry;
import org.joml.Vector2f;

public final class ExplosionEffects {
    private static final ParticleEffect shipDestroySmall = ParticleEffectsRegistry.INSTANCE.getEffectByPath("explosion/ship_small");
    private static final ParticleEffect smallExplosion = ParticleEffectsRegistry.INSTANCE.getEffectByPath("explosion/small");

    public static void spawnDestroyShipSmall(Ship ship) {
        Vector2f scale = ship.getScale();
        Vector2f pos = ship.getPosition();
        Vector2f velocity = ship.getVelocity();
        shipDestroySmall.play(pos.x, pos.y, scale.x, scale.y, velocity.x, velocity.y);
    }

    public static void spawnSmallExplosion(float x, float y, float size) {
        smallExplosion.play(x, y, size, size);
    }
}
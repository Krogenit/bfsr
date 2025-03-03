package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.entity.RigidBody;
import org.jbox2d.common.Vector2;

public class ExplosionEffects {
    private final ParticleEffect shipDestroySmall;
    private final ParticleEffect smallExplosion;

    ExplosionEffects(ParticleEffectsRegistry effectsRegistry) {
        shipDestroySmall = effectsRegistry.get("explosion/ship_small");
        smallExplosion = effectsRegistry.get("explosion/small");
    }

    public void spawnDestroyShipSmall(RigidBody ship) {
        Vector2 velocity = ship.getLinearVelocity();
        float size = Math.max(ship.getSizeX(), ship.getSizeY()) * 1.1f;
        shipDestroySmall.play(ship.getX(), ship.getY(), size, size, velocity.x, velocity.y);
    }

    public void spawnSmallExplosion(float x, float y, float size) {
        smallExplosion.play(x, y, size, size);
    }
}
package net.bfsr.client.particle.effect;

import net.bfsr.client.Client;
import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.renderer.entity.Render;
import net.bfsr.engine.world.entity.RigidBody;
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
        Render render = Client.get().getEntityRenderer().getRender(ship.getId());
        shipDestroySmall.play(ship.getX(), ship.getY(), render.getZ(), size, size, velocity.x, velocity.y);
    }

    public void spawnSmallExplosion(float x, float y, float z, float size, float velocityX, float velocityY) {
        smallExplosion.play(x, y, z, size, size, velocityX, velocityY);
    }
}
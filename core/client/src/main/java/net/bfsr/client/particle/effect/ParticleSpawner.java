package net.bfsr.client.particle.effect;

import net.bfsr.client.entity.wreck.ShipWreck;
import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.client.particle.Particle;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.ObjectPool;
import org.joml.Vector2f;

import java.util.Random;
import java.util.function.Supplier;

public final class ParticleSpawner {
    public static final Random RAND = new Random();
    public static final ObjectPool<Particle> PARTICLE_POOL = new ObjectPool<>();
    public static final ObjectPool<Wreck> PARTICLE_WREAK_POOL = new ObjectPool<>();
    public static final ObjectPool<ShipWreck> PARTICLE_SHIP_WREAK_POOL = new ObjectPool<>();
    public static final Vector2f CACHED_VECTOR = new Vector2f();
    public static final Supplier<Particle> PARTICLE_SUPPLIER = Particle::new;

    public static void light(float x, float y, float size, float r, float g, float b, float a, float alphaSpeed, boolean alphaFromZero, RenderLayer renderLayer) {
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleLight, x, y, 0, 0, 0, 0, size, size, 0, r, g, b, a,
                alphaSpeed, alphaFromZero, renderLayer);
    }
}
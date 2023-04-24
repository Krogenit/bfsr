package net.bfsr.client.particle.effect;

import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.client.particle.Particle;
import net.bfsr.util.ObjectPool;
import org.joml.Vector2f;

import java.util.Random;
import java.util.function.Supplier;

public final class ParticleSpawner {
    public static final Random RAND = new Random();
    public static final ObjectPool<Particle> PARTICLE_POOL = new ObjectPool<>();
    public static final ObjectPool<Wreck> PARTICLE_WREAK_POOL = new ObjectPool<>();
    public static final Vector2f CACHED_VECTOR = new Vector2f();
    public static final Supplier<Particle> PARTICLE_SUPPLIER = Particle::new;
}
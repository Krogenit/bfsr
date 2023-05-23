package net.bfsr.client.particle;

import net.bfsr.util.ObjectPool;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class ParticleManager {
    public static final Random RAND = new Random();
    public static final ObjectPool<Particle> PARTICLE_POOL = new ObjectPool<>();
    public static final Vector2f CACHED_VECTOR = new Vector2f();
    public static final Supplier<Particle> PARTICLE_SUPPLIER = Particle::new;

    private final List<Particle> particles = new ArrayList<>();

    public void update() {
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            if (particle.isDead()) {
                particles.remove(i--);
                particle.onRemoved();
            } else {
                particle.update();
            }
        }
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public int getParticlesCount() {
        return particles.size();
    }

    public Particle getParticle(int index) {
        return particles.get(index);
    }

    public void clear() {
        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).onRemoved();
        }

        particles.clear();
    }
}
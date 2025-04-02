package net.bfsr.engine.world.entity;

import net.bfsr.engine.util.ObjectPool;

import java.util.ArrayList;
import java.util.List;

public class ParticleManager {
    private final ObjectPool<Particle> particlePool;
    private final List<Particle> particles = new ArrayList<>();

    public ParticleManager() {
        particlePool = new ObjectPool<>(() -> new Particle(this));
    }

    public void update() {
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            if (particle.isDead()) {
                particles.remove(i--);
                particle.clear();
            } else {
                particle.update();
            }
        }
    }

    public Particle createParticle() {
        return particlePool.get();
    }

    void addParticle(Particle particle) {
        particles.add(particle);
    }

    public void remove(Particle particle) {
        particlePool.returnBack(particle);
    }

    public int getParticlesCount() {
        return particles.size();
    }

    public Particle getParticle(int index) {
        return particles.get(index);
    }

    public void clear() {
        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).clear();
        }

        particles.clear();
    }
}
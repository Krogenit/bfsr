package net.bfsr.client.particle;

import net.bfsr.client.core.Core;
import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.client.renderer.debug.DebugRenderer;
import org.dyn4j.geometry.AABB;

import java.util.ArrayList;
import java.util.List;

public class ParticleManager {
    private final List<Particle> particles = new ArrayList<>();
    private final List<Wreck> particlesWrecks = new ArrayList<>(64);

    public void update() {
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            particle.update();
            if (particle.isDead()) {
                particle.onRemoved();
                particles.remove(i--);
            }
        }

        for (int i = 0; i < particlesWrecks.size(); i++) {
            Wreck wreck = particlesWrecks.get(i);
            wreck.update();
            if (wreck.isDead()) {
                wreck.onRemoved();
                particlesWrecks.remove(i--);
            }
        }
    }

    public void postPhysicsUpdate() {
        for (int i = 0, size = particlesWrecks.size(); i < size; i++) {
            particlesWrecks.get(i).postPhysicsUpdate();
        }
    }

    public void renderDebug(DebugRenderer debugRenderer) {
        for (int i = 0; i < particlesWrecks.size(); i++) {
            debugRenderer.render(particlesWrecks.get(i));
        }
    }

    public void render() {
        AABB aabb = Core.get().getRenderer().getCamera().getBoundingBox();

        for (int i = 0, size = particlesWrecks.size(); i < size; i++) {
            Wreck wreck = particlesWrecks.get(i);
            if (wreck.getAabb().overlaps(aabb)) {
                wreck.render();
            }
        }
    }

    public void renderAdditive() {
        AABB aabb = Core.get().getRenderer().getCamera().getBoundingBox();

        for (int i = 0, size = particlesWrecks.size(); i < size; i++) {
            Wreck wreck = particlesWrecks.get(i);
            if (wreck.getAabb().overlaps(aabb)) {
                wreck.renderAdditive();
            }
        }
    }

    public void addParticle(Wreck wreck) {
        particlesWrecks.add(wreck);
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public int getParticlesCount() {
        return particles.size();
    }

    public int getWreckCount() {
        return particlesWrecks.size();
    }

    public void clear() {
        for (int i = 0; i < particlesWrecks.size(); i++) {
            particlesWrecks.get(i).onRemoved();
        }
        particlesWrecks.clear();
    }
}
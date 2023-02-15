package net.bfsr.client.particle;

import net.bfsr.client.core.Core;
import net.bfsr.client.renderer.debug.DebugRenderer;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.entity.wreck.WreckCommon;

import java.util.ArrayList;
import java.util.List;

public class ParticleManager {
    private final List<Particle> particles = new ArrayList<>();
    private final List<WreckCommon> particlesWrecks = new ArrayList<>(64);

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
            WreckCommon particle = particlesWrecks.get(i);
            particle.update();
            if (particle.isDead()) {
                particle.onRemoved();
                particlesWrecks.remove(i--);
            }
        }
    }

    public void postPhysicsUpdate() {
        for (int i = 0, size = particlesWrecks.size(); i < size; i++) {
            particlesWrecks.get(i).postPhysicsUpdate();
        }
    }

    public void renderDebug() {
        for (int i = 0; i < particlesWrecks.size(); i++) {
            DebugRenderer.INSTANCE.render(particlesWrecks.get(i));
        }
    }

    public void render() {
        AxisAlignedBoundingBox cameraAABB = Core.get().getRenderer().getCamera().getBoundingBox();

        for (int i = 0, size = particlesWrecks.size(); i < size; i++) {
            WreckCommon wreck = particlesWrecks.get(i);
            if (wreck.getWorldAABB().isIntersects(cameraAABB)) {
                wreck.render();
            }
        }
    }

    public void renderAdditive() {
        AxisAlignedBoundingBox cameraAABB = Core.get().getRenderer().getCamera().getBoundingBox();

        for (int i = 0, size = particlesWrecks.size(); i < size; i++) {
            WreckCommon wreck = particlesWrecks.get(i);
            if (wreck.getWorldAABB().isIntersects(cameraAABB)) {
                wreck.renderAdditive();
            }
        }
    }

    public void addParticle(WreckCommon wreck) {
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

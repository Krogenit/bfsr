package net.bfsr.client.particle;

import net.bfsr.client.Client;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.util.ObjectPool;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class ParticleManager {
    public static final ObjectPool<Particle> PARTICLE_POOL = new ObjectPool<>(Particle::new);
    public static final Vector2f CACHED_VECTOR = new Vector2f();

    private final List<Particle> particles = new ArrayList<>();

    public void init() {
        Client.get().getEventBus().register(this);
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

    void addParticle(Particle particle) {
        particles.add(particle);
    }

    public int getParticlesCount() {
        return particles.size();
    }

    public Particle getParticle(int index) {
        return particles.get(index);
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> clear();
    }

    public void clear() {
        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).clear();
        }

        particles.clear();
    }
}
package net.bfsr.client.particle;

import net.bfsr.client.Core;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.engine.util.ObjectPool;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Listener
public class ParticleManager {
    public static final Random RAND = new Random();
    public static final ObjectPool<Particle> PARTICLE_POOL = new ObjectPool<>(Particle::new);
    public static final Vector2f CACHED_VECTOR = new Vector2f();

    private final List<Particle> particles = new ArrayList<>();

    public void init() {
        Core.get().subscribe(this);
    }

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

    void addParticle(Particle particle) {
        particles.add(particle);
    }

    public int getParticlesCount() {
        return particles.size();
    }

    public Particle getParticle(int index) {
        return particles.get(index);
    }

    @Handler
    public void event(ExitToMainMenuEvent event) {
        clear();
    }

    public void clear() {
        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).onRemoved();
        }

        particles.clear();
    }
}
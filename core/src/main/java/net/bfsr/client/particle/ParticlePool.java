package net.bfsr.client.particle;

import java.util.Stack;
import java.util.function.Supplier;

public class ParticlePool<T extends Particle> {
    private final Stack<T> particles = new Stack<>();

    public T getOrCreate(Supplier<T> supplier) {
        if (particles.empty()) {
            return supplier.get();
        } else {
            return particles.pop();
        }
    }

    public void returnBack(T particle) {
        particles.push(particle);
    }
}

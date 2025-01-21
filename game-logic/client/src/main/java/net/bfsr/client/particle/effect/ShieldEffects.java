package net.bfsr.client.particle.effect;

import net.bfsr.client.Client;
import net.bfsr.client.config.particle.ParticleEffect;

public final class ShieldEffects {
    private static final ParticleEffect rebuild = Client.get().getParticleEffect("shield/rebuild");
    private static final ParticleEffect disable = Client.get().getParticleEffect("shield/disable");

    public static void rebuild(float x, float y, float size, float r, float g, float b, float a) {
        rebuild.play(x, y, size, r, g, b, a);
    }

    public static void disable(float x, float y, float size, float r, float g, float b, float a) {
        disable.play(x, y, size, r, g, b, a);
    }
}
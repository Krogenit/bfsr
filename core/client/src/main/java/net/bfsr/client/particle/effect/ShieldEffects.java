package net.bfsr.client.particle.effect;

import net.bfsr.client.particle.config.ParticleEffect;
import net.bfsr.client.particle.config.ParticleEffectsRegistry;

public final class ShieldEffects {
    private static final ParticleEffect rebuild = ParticleEffectsRegistry.INSTANCE.get("shield/rebuild");
    private static final ParticleEffect disable = ParticleEffectsRegistry.INSTANCE.get("shield/disable");

    public static void rebuild(float x, float y, float size, float r, float g, float b, float a) {
        rebuild.play(x, y, size, r, g, b, a);
    }

    public static void disable(float x, float y, float size, float r, float g, float b, float a) {
        disable.play(x, y, size, r, g, b, a);
    }
}
package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;

public class ShieldEffects {
    private final ParticleEffect rebuild;
    private final ParticleEffect disable;

    ShieldEffects(ParticleEffectsRegistry effectsRegistry) {
        rebuild = effectsRegistry.get("shield/rebuild");
        disable = effectsRegistry.get("shield/disable");
    }

    public void rebuild(float x, float y, float size, float r, float g, float b, float a) {
        rebuild.play(x, y, size, r, g, b, a);
    }

    public void disable(float x, float y, float size, float r, float g, float b, float a) {
        disable.play(x, y, size, r, g, b, a);
    }
}
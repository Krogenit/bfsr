package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;

public class JumpEffects {
    private final ParticleEffect jump;

    JumpEffects(ParticleEffectsRegistry effectsRegistry) {
        jump = effectsRegistry.get("jump");
    }

    public void jump(float x, float y, float size, float velocityX, float velocityY, float r, float g, float b, float a) {
        jump.play(x, y, size, velocityX, velocityY, r, g, b, a);
    }
}
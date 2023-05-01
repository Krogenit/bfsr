package net.bfsr.client.particle.effect;

import net.bfsr.client.particle.config.ParticleEffect;
import net.bfsr.client.particle.config.ParticleEffectsRegistry;

public final class JumpEffects {
    private static final ParticleEffect jump = ParticleEffectsRegistry.INSTANCE.get("jump");

    public static void jump(float x, float y, float size, float velocityX, float velocityY, float r, float g, float b, float a) {
        jump.play(x, y, size, velocityX, velocityY, r, g, b, a);
    }
}
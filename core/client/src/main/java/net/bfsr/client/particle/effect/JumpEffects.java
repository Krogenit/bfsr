package net.bfsr.client.particle.effect;

import net.bfsr.client.particle.ParticleEffect;
import net.bfsr.client.particle.ParticleEffectsRegistry;

public final class JumpEffects {
    private static final ParticleEffect jump = ParticleEffectsRegistry.INSTANCE.getEffectByPath("jump");

    public static void jump(float x, float y, float size, float velocityX, float velocityY, float r, float g, float b, float a) {
        jump.play(x, y, size, velocityX, velocityY, r, g, b, a);
    }
}
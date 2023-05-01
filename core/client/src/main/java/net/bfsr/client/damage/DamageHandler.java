package net.bfsr.client.damage;

import net.bfsr.client.core.Core;
import net.bfsr.client.renderer.Renderer;
import net.bfsr.damage.Damageable;

import java.nio.ByteBuffer;

public final class DamageHandler {
    private static final Renderer RENDERER = Core.get().getRenderer();

    public static void updateDamage(Damageable damageable, int x, int y, int width, int height, ByteBuffer byteBuffer) {
        RENDERER.getRender(damageable.getId()).updateDamageMask(x, y, width, height, byteBuffer);
    }
}
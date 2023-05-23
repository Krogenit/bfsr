package net.bfsr.client.damage;

import net.bfsr.client.Core;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.damage.Damageable;

import java.nio.ByteBuffer;

public final class DamageHandler {
    private static final RenderManager RENDER_MANAGER = Core.get().getWorldRenderer().getRenderManager();

    public static void updateDamage(Damageable damageable, int x, int y, int width, int height, ByteBuffer byteBuffer) {
        RENDER_MANAGER.getRender(damageable.getId()).updateDamageMask(x, y, width, height, byteBuffer);
    }
}
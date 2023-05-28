package net.bfsr.client.damage;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.damage.Damageable;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public final class DamageHandler {
    private final RenderManager renderManager;

    public void updateDamage(Damageable damageable, int x, int y, int width, int height, ByteBuffer byteBuffer) {
        renderManager.getRender(damageable.getId()).updateDamageMask(x, y, width, height, byteBuffer);
    }
}
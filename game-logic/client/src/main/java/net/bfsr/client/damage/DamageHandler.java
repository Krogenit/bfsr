package net.bfsr.client.damage;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.entity.DamageableRigidBodyRenderer;
import net.bfsr.damage.DamageableRigidBody;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public final class DamageHandler {
    private final RenderManager renderManager;

    public void updateDamage(DamageableRigidBody<?> rigidBody, int x, int y, int width, int height, ByteBuffer byteBuffer) {
        Render<?> render = renderManager.getRender(rigidBody.getId());
        if (render instanceof DamageableRigidBodyRenderer<?> damageableRigidBodyRenderer) {
            damageableRigidBodyRenderer.updateDamageMask(x, y, width, height, byteBuffer);
        }
    }
}
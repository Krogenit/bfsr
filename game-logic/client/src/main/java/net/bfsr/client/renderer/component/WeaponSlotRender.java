package net.bfsr.client.renderer.component;

import net.bfsr.client.renderer.Render;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import org.joml.Vector2f;

public class WeaponSlotRender<T extends WeaponSlot> extends Render<T> {
    public WeaponSlotRender(T object) {
        super(Engine.assetsManager.getTexture(object.getGunData().getTexture()), object);
    }

    @Override
    public void update() {
        lastPosition.set(object.getPosition());
    }

    public void renderAlpha(float lastSin, float lastCos, float sin, float cos) {
        Vector2f position = object.getPosition();
        Vector2f scale = object.getSize();
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin,
                cos, scale.x, scale.y, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
    }

    public void renderAdditive(float lastSin, float lastCos, float sin, float cos) {}

    @Override
    public void renderAlpha() {
        throw new UnsupportedOperationException("Use renderAlpha with params instead");
    }

    @Override
    public void renderAdditive() {
        throw new UnsupportedOperationException("Use renderAdditive with params instead");
    }

    public void onShot() {}
}
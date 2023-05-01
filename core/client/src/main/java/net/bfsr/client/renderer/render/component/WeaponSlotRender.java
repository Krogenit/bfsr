package net.bfsr.client.renderer.render.component;

import net.bfsr.client.renderer.SpriteRenderer;
import net.bfsr.client.renderer.buffer.BufferType;
import net.bfsr.client.renderer.render.Render;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.component.weapon.WeaponSlot;
import org.joml.Vector2f;

public class WeaponSlotRender<T extends WeaponSlot> extends Render<T> {
    public WeaponSlotRender(T object) {
        super(TextureLoader.getTexture(object.getGunData().getTexturePath()), object);
    }

    @Override
    public void update() {
        lastPosition.set(object.getPosition());
    }

    public void renderAlpha(float lastSin, float lastCos, float sin, float cos) {
        Vector2f position = object.getPosition();
        Vector2f scale = object.getSize();
        SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos,
                scale.x, scale.y, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
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
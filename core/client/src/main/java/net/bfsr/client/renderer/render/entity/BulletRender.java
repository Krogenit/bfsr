package net.bfsr.client.renderer.render.entity;

import net.bfsr.client.renderer.SpriteRenderer;
import net.bfsr.client.renderer.buffer.BufferType;
import net.bfsr.client.renderer.render.Render;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.texture.TextureRegister;
import org.joml.Vector2f;

public class BulletRender extends Render<Bullet> {
    private static final Texture LIGHT_TEXTURE = TextureLoader.getTexture(TextureRegister.particleLight);

    public BulletRender(Bullet bullet) {
        super(TextureLoader.getTexture(bullet.getBulletData().getTexturePath()), bullet, bullet.getBulletData().getColor().x,
                bullet.getBulletData().getColor().y, bullet.getBulletData().getColor().z, bullet.getBulletData().getColor().w);
    }

    @Override
    public void update() {
        lastPosition.set(object.getPosition());
        lastColor.w = color.w;
        color.w = object.getLifeTime() / object.getStartLifeTime();
    }

    @Override
    public void postWorldUpdate() {
        updateAABB(object.getSin(), object.getCos());
    }

    @Override
    public void renderAdditive() {
        Vector2f position = object.getPosition();
        float sin = object.getSin();
        float cos = object.getCos();
        Vector2f scale = object.getSize();
        float lightSize = 6.0f;
        SpriteRenderer.get().add(lastPosition.x, lastPosition.y, position.x, position.y, lightSize, lightSize,
                color.x / 1.5f, color.y / 1.5f, color.z / 1.5f, color.w / 4.0f, LIGHT_TEXTURE, BufferType.ENTITIES_ADDITIVE);
        SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, sin, cos, scale.x, scale.y,
                color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ADDITIVE);
    }
}
package net.bfsr.client.renderer.entity;

import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.bullet.Bullet;
import org.joml.Vector2f;

public class BulletRender extends RigidBodyRender<Bullet> {
    private static final AbstractTexture LIGHT_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.particleLight);

    public BulletRender(Bullet bullet) {
        super(Engine.assetsManager.getTexture(bullet.getConfigData().getBulletTexture()), bullet,
                bullet.getConfigData().getColor().x,
                bullet.getConfigData().getColor().y, bullet.getConfigData().getColor().z, bullet.getConfigData().getColor().w);
    }

    @Override
    public void update() {
        lastPosition.set(object.getPosition());
        lastColor.w = color.w;
        color.w = 1.0f - object.getLifeTime() / object.getMaxLifeTime();
    }

    @Override
    public void renderAdditive() {
        Vector2f position = object.getPosition();
        float sin = object.getSin();
        float cos = object.getCos();
        Vector2f scale = object.getSize();
        float lightSize = 6.0f;
        spriteRenderer.add(lastPosition.x, lastPosition.y, position.x, position.y, lightSize, lightSize,
                color.x / 1.5f, color.y / 1.5f, color.z / 1.5f, color.w / 4.0f, LIGHT_TEXTURE, BufferType.ENTITIES_ADDITIVE);
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, sin, cos, scale.x,
                scale.y, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ADDITIVE);
    }
}
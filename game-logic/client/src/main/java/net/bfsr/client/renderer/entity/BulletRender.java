package net.bfsr.client.renderer.entity;

import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.bullet.Bullet;

public class BulletRender extends RigidBodyRender {
    private static final AbstractTexture LIGHT_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.particleLight);

    private final Bullet bullet;

    public BulletRender(Bullet bullet) {
        super(Engine.assetsManager.getTexture(bullet.getGunData().getBulletTexture()), bullet, bullet.getGunData().getColor().x,
                bullet.getGunData().getColor().y, bullet.getGunData().getColor().z, bullet.getGunData().getColor().w);
        this.bullet = bullet;
    }

    @Override
    public void update() {
        lastPosition.set(object.getX(), object.getY());
        lastColor.w = color.w;
        color.w = 1.0f - bullet.getLifeTime() / (float) bullet.getMaxLifeTime();
    }

    @Override
    public void renderAlpha() {}

    @Override
    public void renderAdditive() {
        float sin = bullet.getSin();
        float cos = bullet.getCos();
        float lightSize = 6.0f;
        float colorAlpha = lastColor.w + (color.w - lastColor.w) * Engine.renderer.getInterpolation();
        spriteRenderer.add(lastPosition.x, lastPosition.y, object.getX(), object.getY(), lightSize, lightSize,
                color.x / 1.5f, color.y / 1.5f, color.z / 1.5f, colorAlpha / 4.0f, LIGHT_TEXTURE, BufferType.ENTITIES_ADDITIVE);
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, object.getX(), object.getY(), sin, cos, object.getSizeX(),
                object.getSizeY(), color.x, color.y, color.z, colorAlpha, texture, BufferType.ENTITIES_ADDITIVE);
    }
}
package net.bfsr.client.renderer.entity;

import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.bullet.Bullet;

public class BulletRender extends RigidBodyRender {
    private static final AbstractTexture LIGHT_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.particleLight);

    private final Bullet bullet;

    private int lightId = -1;

    public BulletRender(Bullet bullet) {
        super(Engine.assetsManager.getTexture(bullet.getGunData().getBulletTexture()), bullet, bullet.getGunData().getColor().x,
                bullet.getGunData().getColor().y, bullet.getGunData().getColor().z, bullet.getGunData().getColor().w);
        this.bullet = bullet;
    }

    @Override
    public void init() {
        float lightSize = 6.0f;
        lightId = spriteRenderer.add(object.getX(), object.getY(), lightSize, lightSize, color.x / 1.5f, color.y / 1.5f, color.z / 1.5f,
                color.w / 4.0f, LIGHT_TEXTURE.getTextureHandle(), BufferType.ENTITIES_ADDITIVE);
        id = spriteRenderer.add(rigidBody.getX(), rigidBody.getY(), rigidBody.getSin(), rigidBody.getCos(), rigidBody.getSizeX(),
                rigidBody.getSizeY(), color.x, color.y, color.z, color.w, texture.getTextureHandle(), BufferType.ENTITIES_ADDITIVE);
    }

    @Override
    public void update() {
        super.update();
        color.w = 1.0f - bullet.getLifeTime() / (float) bullet.getMaxLifeTime();
    }

    @Override
    protected void updateAABB() {
        super.updateAABB();
        aabb.combine(-3.0f, -3.0f, 3.0f, 3.0f);
    }

    @Override
    protected void updateLastRenderValues() {
        spriteRenderer.setLastPosition(id, BufferType.ENTITIES_ADDITIVE, object.getX(), object.getY());
        spriteRenderer.setLastRotation(id, BufferType.ENTITIES_ADDITIVE, rigidBody.getSin(), rigidBody.getCos());
        spriteRenderer.setLastColorAlpha(id, BufferType.ENTITIES_ADDITIVE, color.w);
        spriteRenderer.setLastPosition(lightId, BufferType.ENTITIES_ADDITIVE, object.getX(), object.getY());
        spriteRenderer.setLastColorAlpha(lightId, BufferType.ENTITIES_ADDITIVE, color.w * 0.25f);
    }

    @Override
    protected void updateRenderValues() {
        spriteRenderer.setPosition(id, BufferType.ENTITIES_ADDITIVE, rigidBody.getX(), rigidBody.getY());
        spriteRenderer.setPosition(lightId, BufferType.ENTITIES_ADDITIVE, rigidBody.getX(), rigidBody.getY());
        spriteRenderer.setRotation(id, BufferType.ENTITIES_ADDITIVE, rigidBody.getSin(), rigidBody.getCos());
        spriteRenderer.setColorAlpha(id, BufferType.ENTITIES_ADDITIVE, color.w);
        spriteRenderer.setColorAlpha(lightId, BufferType.ENTITIES_ADDITIVE, color.w * 0.25f);
    }

    @Override
    public void renderAlpha() {}

    @Override
    public void renderAdditive() {
        spriteRenderer.addDrawCommand(lightId, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.ENTITIES_ADDITIVE);
        spriteRenderer.addDrawCommand(id, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.ENTITIES_ADDITIVE);
    }

    @Override
    public void clear() {
        spriteRenderer.removeObject(id, BufferType.ENTITIES_ADDITIVE);
        spriteRenderer.removeObject(lightId, BufferType.ENTITIES_ADDITIVE);
    }
}
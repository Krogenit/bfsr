package net.bfsr.client.entity.bullet;

import lombok.Getter;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.CollisionObjectUtils;
import net.bfsr.util.TimeUtils;
import org.joml.Vector2f;

public abstract class Bullet extends CollisionObject {
    private static final Texture LIGHT_TEXTURE = TextureLoader.getTexture(TextureRegister.particleLight);

    @Getter
    protected final Ship ship;
    private final float bulletSpeed;
    private final float alphaReducer;
    @Getter
    private final BulletDamage damage;

    protected Bullet(WorldClient world, int id, float bulletSpeed, float x, float y, float sin, float cos, float scaleX, float scaleY, Ship ship,
                     TextureRegister texture, float r, float g, float b, float a, float alphaReducer, BulletDamage damage) {
        super(world, id, x, y, sin, cos, scaleX, scaleY, r, g, b, a, TextureLoader.getTexture(texture));
        this.alphaReducer = alphaReducer;
        this.damage = damage;
        this.ship = ship;
        this.bulletSpeed = bulletSpeed;
        init();
        velocity.set(cos * bulletSpeed, sin * bulletSpeed);
        body.setLinearVelocity(velocity.x, velocity.y);
        world.addBullet(this);
    }

    @Override
    public void update() {
        lastPosition.set(getPosition());

        color.w -= alphaReducer * TimeUtils.UPDATE_DELTA_TIME;

        if (color.w <= 0) {
            setDead();
        }
    }

    @Override
    public void postPhysicsUpdate() {
        position.x = (float) body.getTransform().getTranslationX();
        position.y = (float) body.getTransform().getTranslationY();
        updateAABB();
    }

    @Override
    public void updateClientPositionFromPacket(Vector2f position, float angle, Vector2f velocity, float angularVelocity) {
        setRotation(angle);
        CollisionObjectUtils.updatePos(this, position);
        body.setLinearVelocity(velocity.x, velocity.y);
    }

    @Override
    public boolean canCollideWith(GameObject gameObject) {
        return false;
    }

    public void render() {
        float lightSize = 6.0f;
        SpriteRenderer.get().add(lastPosition.x, lastPosition.y, position.x, position.y, lightSize, lightSize,
                color.x / 1.5f, color.y / 1.5f, color.z / 1.5f, color.w / 4.0f, LIGHT_TEXTURE, BufferType.ENTITIES_ADDITIVE);
        SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, sin, cos, scale.x, scale.y, color.x, color.y, color.z, color.w,
                texture, BufferType.ENTITIES_ADDITIVE);
    }
}
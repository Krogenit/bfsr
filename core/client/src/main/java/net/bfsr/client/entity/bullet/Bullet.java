package net.bfsr.client.entity.bullet;

import lombok.Getter;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.particle.spawner.ParticleSpawner;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.client.world.WorldClient;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.CollisionObjectUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.util.Random;

public abstract class Bullet extends CollisionObject {
    private static final Texture LIGHT_TEXTURE = TextureLoader.getTexture(TextureRegister.particleLight);

    @Getter
    protected final Ship ship;
    private final float bulletSpeed;
    private final float alphaReducer;
    @Getter
    private final BulletDamage damage;
    private float energy;

    protected Bullet(WorldClient world, int id, float bulletSpeed, float x, float y, float sin, float cos, float scaleX, float scaleY, Ship ship,
                     TextureRegister texture, float r, float g, float b, float a, float alphaReducer, BulletDamage damage) {
        super(world, id, x, y, sin, cos, scaleX, scaleY, r, g, b, a, TextureLoader.getTexture(texture));
        this.alphaReducer = alphaReducer;
        this.damage = damage;
        this.ship = ship;
        this.bulletSpeed = bulletSpeed;
        energy = damage.getAverageDamage();
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

    private void onDamageShipWithNoShield() {
        Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.damageNoShield, position.x, position.y));
    }

    private void onDamageShipWithShield() {
        Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.damage, position.x, position.y));
    }

    private void destroyBullet(CollisionObject destroyer, Contact contact, Vector2 normal) {
        Vector2f position = getPosition();
        Vector2 pos1 = contact.getPoint();

        if (destroyer != null) {
            if (destroyer instanceof Bullet) {
                ParticleSpawner.spawnLight(position.x, position.y, getScale().x * 5.0f, 42.0f, color.x, color.y, color.z, 0.5f, 15.0f, true, RenderLayer.DEFAULT_ADDITIVE);
            } else if (destroyer instanceof Wreck) {
                ParticleSpawner.spawnDirectedSpark((float) pos1.x, (float) pos1.y, (float) normal.x, (float) normal.y, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
                Random rand = world.getRand();
                if (rand.nextInt(4) == 0) {
                    RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, CollisionObjectUtils.ANGLE_TO_VELOCITY);
                    ParticleSpawner.spawnShipOst(1, (float) pos1.x, (float) pos1.y, velocity.x + CollisionObjectUtils.ANGLE_TO_VELOCITY.x,
                            velocity.y + CollisionObjectUtils.ANGLE_TO_VELOCITY.y, 0.5f);
                }
                RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 2.5f * (rand.nextFloat() + 0.5f), CollisionObjectUtils.ANGLE_TO_VELOCITY);
                ParticleSpawner.spawnSmallGarbage(1 + rand.nextInt(3), (float) pos1.x, (float) pos1.y, velocity.x + CollisionObjectUtils.ANGLE_TO_VELOCITY.x,
                        velocity.y + CollisionObjectUtils.ANGLE_TO_VELOCITY.y,
                        2.0f * (rand.nextFloat() + 0.5f), 5.0f, 0.5f);
            }
        } else {
            ParticleSpawner.spawnDirectedSpark((float) pos1.x, (float) pos1.y, (float) normal.x, (float) normal.y, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
        }
        ParticleSpawner.spawnLight(position.x, position.y, getScale().x * 3.0f, 18.0f, color.x, color.y, color.z, 0.4f, 30.0f, true, RenderLayer.DEFAULT_ADDITIVE);
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
package net.bfsr.client.entity.bullet;

import lombok.Getter;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.util.CollisionObjectUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
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
    private Object previousAObject;

    protected Bullet(WorldClient world, int id, float bulletSpeed, float x, float y, float sin, float cos, float scaleX, float scaleY, Ship ship,
                     TextureRegister texture, float r, float g, float b, float a, float alphaReducer, BulletDamage damage) {
        super(world, id, x, y, sin, cos, scaleX, scaleY, r, g, b, a, TextureLoader.getTexture(texture));
        this.alphaReducer = alphaReducer;
        this.damage = damage;
        this.ship = ship;
        this.bulletSpeed = bulletSpeed;
        energy = damage.getAverageDamage();
        setBulletVelocityAndStartTransform(x, y);
        world.addBullet(this);
    }

    private void setBulletVelocityAndStartTransform(float x, float y) {
        velocity.set(cos * bulletSpeed, sin * bulletSpeed);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(sin, cos);
        body.getTransform().setTranslation(x + velocity.x / 500.0f, y + velocity.y / 500.0f);//TODO: посчитать точку появления пули правильно
    }

    @Override
    public void update() {
        lastSin = sin;
        lastCos = cos;
        lastPosition.set(getPosition());

        color.w -= alphaReducer * TimeUtils.UPDATE_DELTA_TIME;

        if (color.w <= 0) {
            setDead();
        }
    }

    @Override
    public void postPhysicsUpdate() {
        Vector2 velocity = body.getLinearVelocity();
        float rotateToVector = (float) Math.atan2(-velocity.x, velocity.y) + MathUtils.HALF_PI;
        sin = LUT.sin(rotateToVector);
        cos = LUT.cos(rotateToVector);
        lastSin = sin;
        lastCos = cos;
        body.getTransform().setRotation(sin, cos);
        updateWorldAABB();
    }

    @Override
    public void checkCollision(Contact contact, Vector2 normal, Body body) {
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof Ship ship) {
                if (canDamageShip(ship)) {
                    previousAObject = ship;
                    Vector2f position = getPosition();
                    if (damageShip(ship)) {
                        //Hull damage
                        destroyBullet(ship, contact, normal);
                        setDead();
                        onDamageShipWithNoShield();
                    } else {
                        //Shield reflection
                        destroyBullet(ship, contact, normal);
                        damage(this);
                        onDamageShipWithShield();
                    }
                } else if (previousAObject != null && previousAObject != ship && this.ship == ship) {
                    previousAObject = ship;
                    //We can damage ship after some collission with other object
                    destroyBullet(ship, contact, normal);
                }
            } else if (userData instanceof Bullet bullet) {
                //Bullet vs bullet
                bullet.damage(this);
                previousAObject = bullet;

                if (bullet.isDead()) {
                    bullet.destroyBullet(this, contact, normal);
                }
            } else if (userData instanceof Wreck wreck) {
                wreck.damage(damage.getBulletDamageHull());
                destroyBullet(wreck, contact, normal);
            }
        }
    }

    private void onDamageShipWithNoShield() {
        Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.damageNoShield, position.x, position.y));
    }

    private void onDamageShipWithShield() {
        Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.damage, position.x, position.y));
    }

    private void damage(Bullet bullet) {
        float damage = bullet.damage.getAverageDamage();
        damage /= 3.0f;

        this.damage.reduceBulletDamageArmor(damage);
        this.damage.reduceBulletDamageHull(damage);
        this.damage.reduceBulletDamageShield(damage);

        if (this.damage.getBulletDamageArmor() < 0) setDead();
        else if (this.damage.getBulletDamageHull() < 0) setDead();
        else if (this.damage.getBulletDamageShield() < 0) setDead();

        if (bullet != this) {
            energy -= damage;

            if (energy <= 0) {
                setDead();
            }
        }
    }

    private void destroyBullet(CollisionObject destroyer, Contact contact, Vector2 normal) {
        Vector2f position = getPosition();
        if (destroyer != null) {
            if (destroyer instanceof Ship ship) {
                ShieldCommon shield = ship.getShield();
                if (shield == null || shield.getShield() <= 0) {
                    Hull hull = ship.getHull();
                    Vector2 pos1 = contact.getPoint();
                    float velocityX = destroyer.getVelocity().x * 0.005f;
                    float velocityY = destroyer.getVelocity().y * 0.005f;
                    Random rand = world.getRand();
                    ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
                    if (hull.getHull() / hull.getMaxHull() < 0.5f && rand.nextInt(2) == 0) {
                        RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, CollisionObjectUtils.ANGLE_TO_VELOCITY);
                        ParticleSpawner.spawnShipOst(1, (float) pos1.x, (float) pos1.y, velocityX + CollisionObjectUtils.ANGLE_TO_VELOCITY.x,
                                velocityY + CollisionObjectUtils.ANGLE_TO_VELOCITY.y, 0.5f);
                    }
                    Vector2f angleToVelocity = RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 2.5f * (rand.nextFloat() + 0.5f));
                    ParticleSpawner.spawnSmallGarbage(1 + rand.nextInt(3), (float) pos1.x, (float) pos1.y, velocityX + angleToVelocity.x, velocityY + angleToVelocity.y,
                            2.0f * (rand.nextFloat() + 0.5f), 5.0f, 0.5f);
                }

                ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
            } else if (destroyer instanceof Bullet) {
                ParticleSpawner.spawnLight(position.x, position.y, getScale().x * 5.0f, 7.0f * 6.0f, color.x, color.y, color.z, 0.5f, 0.25f * 60.0f, true, RenderLayer.DEFAULT_ADDITIVE);
            } else if (destroyer instanceof Wreck) {
                Vector2 pos1 = contact.getPoint();
                ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
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
            ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
        }
        ParticleSpawner.spawnLight(position.x, position.y, getScale().x * 3.0f, 3.0f * 6.0f, color.x, color.y, color.z, 0.4f, 0.5f * 60.0f, true, RenderLayer.DEFAULT_ADDITIVE);
    }

    public boolean canDamageShip(Ship ship) {
        return this.ship != ship && previousAObject != ship;
    }

    @Override
    public boolean canCollideWith(GameObject gameObject) {
        return ship != gameObject && previousAObject != gameObject;
    }

    private boolean damageShip(Ship ship) {
        return ship.attackShip(damage, ship, getPosition(), ship.getFaction() == ship.getFaction() ? 0.5f : 1.0f);
    }

    public void render() {
        float size = 6.0f;
        Vector2f pos = getPosition();
        SpriteRenderer.INSTANCE.addToRenderPipeLine(lastPosition.x, lastPosition.y, pos.x, pos.y, size, size,
                color.x / 1.5f, color.y / 1.5f, color.z / 1.5f, color.w / 4.0f, LIGHT_TEXTURE, BufferType.ENTITIES_ADDITIVE);
        SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, pos.x, pos.y, sin, cos, scale.x, scale.y, color.x, color.y, color.z, color.w,
                texture, BufferType.ENTITIES_ADDITIVE);
    }
}

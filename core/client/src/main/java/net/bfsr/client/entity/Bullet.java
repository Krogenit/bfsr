package net.bfsr.client.entity;

import net.bfsr.client.core.Core;
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
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.BulletCommon;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.util.Random;

public abstract class Bullet extends BulletCommon {
    private static final Texture LIGHT_TEXTURE = TextureLoader.getTexture(TextureRegister.particleLight);
    private final Texture texture;

    protected Bullet(WorldClient world, int id, float bulletSpeed, float sin, float cos, float x, float y, float scaleX, float scaleY, ShipCommon ship, TextureRegister texture,
                     float r, float g, float b, float a, float alphaReducer, BulletDamage damage) {
        super(world, id, bulletSpeed, x, y, sin, cos, scaleX, scaleY, ship, r, g, b, a, alphaReducer, damage);
        this.texture = TextureLoader.getTexture(texture);
    }

    @Override
    public void update() {
        super.update();
        aliveTimer = 0;
    }

    @Override
    protected void onDamageShipWithNoShield() {
        Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.damageNoShield, position.x, position.y));
    }

    @Override
    protected void onDamageShipWithShield() {
        Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.damage, position.x, position.y));
    }

    @Override
    protected void destroyBullet(CollisionObject destroyer, Contact contact, Vector2 normal) {
        Vector2f position = getPosition();
        if (destroyer != null) {
            if (destroyer instanceof ShipCommon ship) {
                ShieldCommon shield = ship.getShield();
                if (shield == null || shield.getShield() <= 0) {
                    Hull hull = ship.getHull();
                    Vector2 pos1 = contact.getPoint();
                    float velocityX = destroyer.getVelocity().x * 0.005f;
                    float velocityY = destroyer.getVelocity().y * 0.005f;
                    Random rand = world.getRand();
                    ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
                    if (hull.getHull() / hull.getMaxHull() < 0.5f && rand.nextInt(2) == 0) {
                        RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, angleToVelocity);
                        ParticleSpawner.spawnShipOst(1, (float) pos1.x, (float) pos1.y, velocityX + angleToVelocity.x, velocityY + angleToVelocity.y, 0.5f);
                    }
                    Vector2f angleToVelocity = RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 2.5f * (rand.nextFloat() + 0.5f));
                    ParticleSpawner.spawnSmallGarbage(1 + rand.nextInt(3), (float) pos1.x, (float) pos1.y, velocityX + angleToVelocity.x, velocityY + angleToVelocity.y,
                            2.0f * (rand.nextFloat() + 0.5f), 5.0f, 0.5f);
                }

                ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
            } else if (destroyer instanceof BulletCommon) {
                ParticleSpawner.spawnLight(position.x, position.y, getScale().x * 5.0f, 7.0f * 6.0f, color.x, color.y, color.z, 0.5f, 0.25f * 60.0f, true, RenderLayer.DEFAULT_ADDITIVE);
            } else if (destroyer instanceof Wreck) {
                Vector2 pos1 = contact.getPoint();
                ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
                Random rand = world.getRand();
                if (rand.nextInt(4) == 0) {
                    RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, angleToVelocity);
                    ParticleSpawner.spawnShipOst(1, (float) pos1.x, (float) pos1.y, velocity.x + angleToVelocity.x, velocity.y + angleToVelocity.y, 0.5f);
                }
                RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 2.5f * (rand.nextFloat() + 0.5f), angleToVelocity);
                ParticleSpawner.spawnSmallGarbage(1 + rand.nextInt(3), (float) pos1.x, (float) pos1.y, velocity.x + angleToVelocity.x, velocity.y + angleToVelocity.y,
                        2.0f * (rand.nextFloat() + 0.5f), 5.0f, 0.5f);
            }
        } else {
            ParticleSpawner.spawnDirectedSpark(contact, normal, getScale().x * 1.5f, color.x, color.y, color.z, color.w);
        }
        ParticleSpawner.spawnLight(position.x, position.y, getScale().x * 3.0f, 3.0f * 6.0f, color.x, color.y, color.z, 0.4f, 0.5f * 60.0f, true, RenderLayer.DEFAULT_ADDITIVE);
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

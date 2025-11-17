package net.bfsr.client.renderer.entity;

import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;
import net.bfsr.client.Client;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.client.particle.effect.FireEffects;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.world.entity.SpawnAccumulator;
import net.bfsr.entity.wreck.Wreck;

import java.nio.ByteBuffer;

public class WreckRender extends RigidBodyRender {
    private final FireEffects fireEffects = Client.get().getParticleEffects().getFireEffects();
    private final ExplosionEffects explosionEffects = Client.get().getParticleEffects().getExplosionEffects();
    private final XoRoShiRo128PlusPlusRandom random = new XoRoShiRo128PlusPlusRandom();
    private final SpawnAccumulator spawnAccumulator = new SpawnAccumulator();

    private final Wreck wreck;
    private final float fireAnimationSpeed = Engine.convertToDeltaTime(0.18f);
    private float fireAmount;
    private boolean fireFadingOut;
    private final DamageMaskTexture maskTexture;
    private final ByteBuffer byteBuffer;

    public WreckRender(Wreck wreck, float z) {
        super(wreck, z, Engine.getAssetsManager().getTexture(wreck.getConfigData().getTextureData()), 0.5f, 0.5f, 0.5f, 1.0f);
        this.wreck = wreck;

        if (wreck.isEmitFire()) {
            spawnAccumulator.resetTime();
        }

        fireAmount = 3.0f;
        int damageMaskWidth = texture.getWidth() / 2;
        int damageMaskHeight = texture.getHeight() / 2;
        this.maskTexture = new DamageMaskTexture(damageMaskWidth, damageMaskHeight);
        this.maskTexture.createEmpty();
        byteBuffer = renderer.createByteBuffer(damageMaskWidth * damageMaskHeight);
        for (int j = 0; j < damageMaskHeight; j++) {
            for (int i = 0; i < damageMaskWidth; i++) {
                int index = j * damageMaskWidth + i;
                byteBuffer.put(index, (byte) random.nextInt(256));
            }
        }

        maskTexture.upload(0, 0, damageMaskWidth, damageMaskHeight, byteBuffer);
    }

    @Override
    public void init() {
        id = spriteRenderer.add(rigidBody.getX(), rigidBody.getY(), z, rigidBody.getSin(), rigidBody.getCos(),
                rigidBody.getSizeX(), rigidBody.getSizeY(), color.x, color.y, color.z, color.w, texture.getTextureHandle(),
                maskTexture.getTextureHandle(), BufferType.ENTITIES_ALPHA);

        spriteRenderer.setLastFireAmount(id, BufferType.ENTITIES_ALPHA, fireAmount);
        spriteRenderer.setLastFireUVAnimation(id, BufferType.ENTITIES_ALPHA, maskTexture.getFireUVAnimation());
        spriteRenderer.setFireAmount(id, BufferType.ENTITIES_ALPHA, fireAmount);
        spriteRenderer.setFireUVAnimation(id, BufferType.ENTITIES_ALPHA, maskTexture.getFireUVAnimation());
    }

    @Override
    public void update() {
        super.update();
        maskTexture.updateEffects();

        updateLifeTime();
        updateFireAndExplosion();
    }

    @Override
    protected void updateLastRenderValues() {
        super.updateLastRenderValues();

        if (fireAmount > 0) {
            spriteRenderer.setLastFireAmount(id, BufferType.ENTITIES_ALPHA, fireAmount);
            spriteRenderer.setLastFireUVAnimation(id, BufferType.ENTITIES_ALPHA, maskTexture.getFireUVAnimation());
            spriteRenderer.setLastColorAlpha(id, BufferType.ENTITIES_ALPHA, color.w);
        }
    }

    @Override
    protected void updateRenderValues() {
        super.updateRenderValues();
        spriteRenderer.setColorAlpha(id, BufferType.ENTITIES_ALPHA, color.w);
    }

    private void updateLifeTime() {
        color.w = 1.0f - wreck.getLifeTime() / (float) wreck.getMaxLifeTime();
    }

    private void emitFire() {
        if (color.w > 0.6f) {
            fireEffects.emitFire(object.getX(), object.getY(), z, spawnAccumulator);
        }
    }

    private void updateFireAndExplosion() {
        if (wreck.isEmitFire()) {
            emitFire();
        }

        updateFire();
    }

    private void updateFire() {
        if (fireFadingOut) {
            if (fireAmount > 0.4f) {
                fireAmount -= fireAnimationSpeed;
                if (fireAmount < 0.0f) {
                    fireAmount = 0.0f;
                }
            } else {
                fireFadingOut = false;
            }
        } else {
            if (fireAmount < 1.0f) {
                fireAmount += fireAnimationSpeed;
            } else {
                fireFadingOut = true;
            }
        }

        spriteRenderer.setFireAmount(id, BufferType.ENTITIES_ALPHA, fireAmount);
        spriteRenderer.setFireUVAnimation(id, BufferType.ENTITIES_ALPHA, maskTexture.getFireUVAnimation());

        if (color.w <= 0.5f) {
            if (fireAmount > 0.0f) {
                float fireSpeed = fireAnimationSpeed * 4.0f;

                fireAmount -= fireSpeed;
                if (fireAmount < 0.0f) {
                    fireAmount = 0.0f;
                }

                spriteRenderer.setFireAmount(id, BufferType.ENTITIES_ALPHA, fireAmount);
                spriteRenderer.setFireUVAnimation(id, BufferType.ENTITIES_ALPHA, maskTexture.getFireUVAnimation());
            }
        }
    }

    public void onDeath() {
        if (color.w > 0.01f) {
            explosionEffects.spawnSmallExplosion(wreck.getX(), wreck.getY(), z, wreck.getSizeX(), 0.0f, 0.0f);
        }
    }

    @Override
    public void clear() {
        super.clear();

        maskTexture.delete();
        renderer.memFree(byteBuffer);
    }
}
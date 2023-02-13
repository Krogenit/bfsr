package net.bfsr.client.particle;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.util.MutableInt;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Particle extends TextureObject {
    @Getter
    protected float sizeVelocity, alphaVelocity, angularVelocity, maxAlpha;
    protected boolean isAlphaFromZero, zeroVelocity;
    @Getter
    protected RenderLayer renderLayer;
    private final Vector2f velocity = new Vector2f();
    @Getter
    @Setter
    private boolean isDead;

    public Particle init(World world, TextureRegister texture, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero, RenderLayer renderLayer) {
        this.texture = TextureLoader.getTexture(texture);
        this.position.set(x, y);
        this.lastPosition.set(position);
        this.velocity.set(velocityX, velocityY);
        this.rotation = rotation;
        this.lastRotation = rotation;
        this.angularVelocity = angularVelocity;
        this.scale.set(scaleX, scaleY);
        this.lastScale.set(scaleX, scaleY);
        this.sizeVelocity = sizeVelocity;
        this.color.set(r, g, b, a);
        this.alphaVelocity = alphaVelocity;
        this.isAlphaFromZero = isAlphaFromZero;
        this.renderLayer = renderLayer;
        this.zeroVelocity = velocity.length() != 0;
        this.isDead = false;

        if (isAlphaFromZero) {
            this.maxAlpha = a;
            this.color.w = 0.0f;
        }

        lastColor.set(color);

        if (world.isRemote()) {
            addParticle();
        }

        return this;
    }

    public Particle init(TextureRegister texture, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY, float sizeVelocity,
                         float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero, RenderLayer renderLayer) {
        return init(Core.get().getWorld(), texture, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity,
                isAlphaFromZero, renderLayer);
    }

    protected void addParticle() {
        Core.get().getWorld().getParticleManager().addParticle(this);
        Core.get().getRenderer().getParticleRenderer().addParticleToRenderLayer(this, renderLayer);
    }

    @Override
    public void update() {
        lastPosition.set(getPosition());
        lastRotation = getRotation();

        position.x += velocity.x * TimeUtils.UPDATE_DELTA_TIME;
        position.y += velocity.y * TimeUtils.UPDATE_DELTA_TIME;
        rotation += angularVelocity * TimeUtils.UPDATE_DELTA_TIME;

        if (!zeroVelocity) {
            velocity.x *= 0.99f * TimeUtils.UPDATE_DELTA_TIME;
            velocity.y *= 0.99f * TimeUtils.UPDATE_DELTA_TIME;
        }

        if (sizeVelocity != 0) {
            lastScale.set(scale);
            float sizeVel = sizeVelocity * TimeUtils.UPDATE_DELTA_TIME;
            scale.add(sizeVel, sizeVel);

            if (scale.x <= 0.0f || scale.y <= 0.0f)
                setDead(true);
        }

        if (alphaVelocity != 0) {
            lastColor.w = color.w;
            if (isAlphaFromZero) {
                if (maxAlpha != 0) {
                    color.w += alphaVelocity * TimeUtils.UPDATE_DELTA_TIME;

                    if (color.w >= maxAlpha) {
                        maxAlpha = 0.0f;
                    }
                } else {
                    color.w -= alphaVelocity * TimeUtils.UPDATE_DELTA_TIME;
                    if (color.w <= 0)
                        setDead(true);
                }
            } else {
                color.w -= alphaVelocity * TimeUtils.UPDATE_DELTA_TIME;
                if (color.w <= 0)
                    setDead(true);
            }
        }
    }

    public void putToBuffer(FloatBuffer vertexBuffer, ByteBuffer materialBuffer, float interpolation, MutableInt vertexBufferIndex, MutableInt materialBufferIndex) {
        SpriteRenderer.INSTANCE.putVertices(lastPosition.x, lastPosition.y, position.x, position.y, lastRotation, rotation, lastScale.x, lastScale.y, scale.x, scale.y,
                interpolation, vertexBuffer, vertexBufferIndex);
        SpriteRenderer.INSTANCE.putColor(lastColor, color, materialBuffer, materialBufferIndex, interpolation);
        SpriteRenderer.INSTANCE.putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    public void onRemoved() {
        ParticleSpawner.PARTICLE_POOL.returnBack(this);
        Core.get().getRenderer().getParticleRenderer().removeParticleFromRenderLayer(this, renderLayer);
    }
}

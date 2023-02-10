package net.bfsr.client.particle;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.render.instanced.InstancedRenderer;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.server.MainServer;
import net.bfsr.util.MutableInt;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Particle extends TextureObject {
    @Getter
    protected float sizeVelocity, alphaVelocity, angularVelocity, maxAlpha;
    protected boolean canCollide, isAlphaFromZero, zeroVelocity;
    @Getter
    protected RenderLayer renderLayer;
    private final Vector2f velocity = new Vector2f();
    @Getter
    @Setter
    private boolean isDead;

    public Particle init(World world, TextureRegister texture, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero, boolean canCollide,
                         RenderLayer renderLayer) {
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
        this.canCollide = canCollide;
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

    public Particle init(float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY, float sizeVelocity,
                         float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero, boolean canCollide, RenderLayer renderLayer) {
        return init(MainServer.getInstance().getWorld(), null, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity,
                isAlphaFromZero, canCollide, renderLayer);
    }

    public Particle init(TextureRegister texture, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero, boolean canCollide,
                         RenderLayer renderLayer) {
        return init(Core.getCore().getWorld(), texture, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity,
                isAlphaFromZero, canCollide, renderLayer);
    }

    public Particle init(TextureRegister texture, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY, float sizeVelocity,
                         float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero, RenderLayer renderLayer) {
        return init(Core.getCore().getWorld(), texture, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity,
                isAlphaFromZero, false, renderLayer);
    }

    protected void addParticle() {
        Core.getCore().getWorld().getParticleManager().addParticle(this);
        Core.getCore().getRenderer().getParticleRenderer().addParticleToRenderLayer(this, renderLayer);
    }

    @Override
    public void update() {
        lastPosition.set(getPosition());
        lastRotation = getRotation();

        if (!canCollide) {
            position.x += velocity.x * TimeUtils.UPDATE_DELTA_TIME;
            position.y += velocity.y * TimeUtils.UPDATE_DELTA_TIME;
            rotation += angularVelocity * TimeUtils.UPDATE_DELTA_TIME;

            if (!zeroVelocity) {
                velocity.x *= 0.99f * TimeUtils.UPDATE_DELTA_TIME;
                velocity.y *= 0.99f * TimeUtils.UPDATE_DELTA_TIME;
            }
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

                    if (color.w >= maxAlpha * 2.0f)
                        setDead(true);

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
        InstancedRenderer.INSTANCE.putVertices(lastPosition.x, lastPosition.y, position.x, position.y, lastRotation, rotation, lastScale.x, lastScale.y, scale.x, scale.y,
                interpolation, vertexBuffer, vertexBufferIndex);
        InstancedRenderer.INSTANCE.putColor(lastColor, color, materialBuffer, materialBufferIndex, interpolation);
        InstancedRenderer.INSTANCE.putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    @Override
    public Vector2f getPosition() {
        if (canCollide) return super.getPosition();
        else return position;
    }

    @Override
    public float getRotation() {
        if (canCollide) return super.getRotation();
        else return rotation;
    }

    public void onRemoved() {
        ParticleSpawner.PARTICLE_POOL.returnBack(this);
        Core.getCore().getRenderer().getParticleRenderer().removeParticleFromRenderLayer(this, renderLayer);
    }
}

package net.bfsr.client.renderer.particle;

import net.bfsr.client.particle.Particle;
import net.bfsr.client.renderer.Render;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.util.MutableInt;
import net.bfsr.engine.util.TimeUtils;
import org.joml.Vector2f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ParticleRender extends Render<Particle> {
    protected long textureHandle;
    protected Vector2f position;
    protected Vector2f size;
    private boolean isAlphaFromZero;
    private float maxAlpha;

    public ParticleRender init(Particle object, long textureHandle, float r, float g, float b, float a, boolean isAlphaFromZero) {
        this.object = object;
        this.position = object.getPosition();
        this.size = object.getSize();
        this.lastPosition.set(position);
        this.lastSin = object.getSin();
        this.lastCos = object.getCos();
        this.lastSize.set(size);
        this.color.set(r, g, b, a);
        this.lastColor.set(color);
        this.textureHandle = textureHandle;
        this.isAlphaFromZero = isAlphaFromZero;

        if (isAlphaFromZero) {
            this.maxAlpha = a;
            this.color.w = 0.0f;
        }

        return this;
    }

    @Override
    public void update() {
        lastPosition.set(object.getPosition());
        lastSin = object.getSin();
        lastCos = object.getCos();

        if (object.getSizeVelocity() != 0) {
            lastSize.set(object.getSize());
        }

        float alphaVelocity = object.getAlphaVelocity();
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
                        object.setDead();
                }
            } else {
                color.w -= alphaVelocity * TimeUtils.UPDATE_DELTA_TIME;
                if (color.w <= 0)
                    object.setDead();
            }
        }
    }

    public void putToBuffer(AbstractSpriteRenderer spriteRenderer, FloatBuffer vertexBuffer, ByteBuffer materialBuffer, float interpolation,
                            MutableInt vertexBufferIndex, MutableInt materialBufferIndex) {
        spriteRenderer.putVertices(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, object.getSin(), object.getCos(),
                lastSize.x, lastSize.y, size.x, size.y, interpolation, vertexBuffer, vertexBufferIndex);
        spriteRenderer.putColor(lastColor, color, materialBuffer, materialBufferIndex, interpolation);
        spriteRenderer.putTextureHandle(textureHandle, materialBuffer, materialBufferIndex);
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, materialBuffer, materialBufferIndex);
    }
}
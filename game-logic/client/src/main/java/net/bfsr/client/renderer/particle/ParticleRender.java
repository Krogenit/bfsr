package net.bfsr.client.renderer.particle;

import net.bfsr.client.particle.Particle;
import net.bfsr.client.renderer.Render;
import net.bfsr.engine.util.MutableInt;
import org.joml.Vector2f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ParticleRender extends Render implements net.bfsr.engine.renderer.particle.ParticleRender {
    private Particle particle;
    private long textureHandle;
    protected Vector2f position;
    protected Vector2f size;
    private boolean isAlphaFromZero;
    private float maxAlpha;

    public ParticleRender init(Particle object, long textureHandle, float r, float g, float b, float a, boolean isAlphaFromZero) {
        this.object = this.particle = object;
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
        lastSin = particle.getSin();
        lastCos = particle.getCos();

        if (particle.getSizeVelocity() != 0) {
            lastSize.set(object.getSize());
        }

        float alphaVelocity = particle.getAlphaVelocity();
        if (alphaVelocity != 0) {
            lastColor.w = color.w;
            if (isAlphaFromZero) {
                if (maxAlpha == 0) {
                    color.w -= alphaVelocity;
                    if (color.w <= 0) object.setDead();
                } else {
                    color.w += alphaVelocity;
                    if (color.w >= maxAlpha) maxAlpha = 0.0f;
                }
            } else {
                color.w -= alphaVelocity;
                if (color.w <= 0) object.setDead();
            }
        }
    }

    @Override
    public void putToBuffer(FloatBuffer vertexBuffer, ByteBuffer materialBuffer, float interpolation,
                            MutableInt vertexBufferIndex, MutableInt materialBufferIndex) {
        spriteRenderer.putVertices(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, particle.getSin(),
                particle.getCos(), lastSize.x, lastSize.y, size.x, size.y, interpolation, vertexBuffer, vertexBufferIndex);
        spriteRenderer.putColor(lastColor, color, materialBuffer, materialBufferIndex, interpolation);
        spriteRenderer.putTextureHandle(textureHandle, materialBuffer, materialBufferIndex);
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, 0, materialBuffer, materialBufferIndex);
    }
}
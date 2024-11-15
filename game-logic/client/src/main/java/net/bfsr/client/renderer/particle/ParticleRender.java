package net.bfsr.client.renderer.particle;

import net.bfsr.client.particle.Particle;
import net.bfsr.client.renderer.Render;
import net.bfsr.engine.util.MutableInt;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ParticleRender extends Render implements net.bfsr.engine.renderer.particle.ParticleRender {
    private Particle particle;
    private long textureHandle;
    private boolean isAlphaFromZero;
    private float maxAlpha;

    public ParticleRender init(Particle object, long textureHandle, float r, float g, float b, float a, boolean isAlphaFromZero) {
        this.object = this.particle = object;
        this.lastPosition.set(object.getX(), object.getY());
        this.lastSin = object.getSin();
        this.lastCos = object.getCos();
        this.lastSize.set(object.getSizeX(), object.getSizeY());
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
        lastPosition.set(object.getX(), object.getY());
        lastSin = particle.getSin();
        lastCos = particle.getCos();

        if (particle.getSizeVelocity() != 0) {
            lastSize.set(object.getSizeX(), object.getSizeY());
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
        spriteRenderer.putVertices(lastPosition.x, lastPosition.y, object.getX(), object.getY(), lastSin, lastCos, particle.getSin(),
                particle.getCos(), lastSize.x, lastSize.y, object.getSizeX(), object.getSizeY(), interpolation, vertexBuffer,
                vertexBufferIndex);
        spriteRenderer.putColor(lastColor, color, materialBuffer, materialBufferIndex, interpolation);
        spriteRenderer.putTextureHandle(textureHandle, materialBuffer, materialBufferIndex);
        spriteRenderer.putMaterialData(0, 0.0f, 0.0f, 0, materialBuffer, materialBufferIndex);
    }
}
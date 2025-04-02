package net.bfsr.engine.renderer.particle;

import lombok.Setter;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.entity.Render;
import net.bfsr.engine.world.entity.Particle;

import java.util.function.Consumer;

public class ParticleRender extends Render {
    private Particle particle;
    private boolean isAlphaFromZero;
    private float maxAlpha;
    private float alphaVelocity;
    private AbstractBuffersHolder buffersHolder;
    @Setter
    private Consumer<ParticleRender> lastValuesUpdateConsumer;

    public ParticleRender init(Particle object, float x, float y, float sin, float cos, float width, float height, long textureHandle,
                               float r, float g, float b, float a, boolean isAlphaFromZero, float alphaVelocity,
                               AbstractBuffersHolder buffersHolder, Consumer<ParticleRender> lastValuesUpdateConsumer) {
        this.buffersHolder = buffersHolder;
        this.object = this.particle = object;
        this.color.set(r, g, b, a);
        this.isAlphaFromZero = isAlphaFromZero;
        this.alphaVelocity = alphaVelocity;
        this.lastValuesUpdateConsumer = lastValuesUpdateConsumer;

        if (isAlphaFromZero) {
            this.maxAlpha = a;
            this.color.w = 0.0f;
        }

        if (id == -1) {
            id = spriteRenderer.add(x, y, sin, cos, width, height, color.x, color.y, color.z, color.w, textureHandle, buffersHolder);
        } else {
            spriteRenderer.setTexture(id, buffersHolder, textureHandle);
            spriteRenderer.setColor(id, buffersHolder, color);
            spriteRenderer.setLastColor(id, buffersHolder, color);

            spriteRenderer.setPosition(id, buffersHolder, x, y);
            spriteRenderer.setLastPosition(id, buffersHolder, x, y);
            spriteRenderer.setRotation(id, buffersHolder, sin, cos);
            spriteRenderer.setLastRotation(id, buffersHolder, sin, cos);
            spriteRenderer.setSize(id, buffersHolder, width, height);
            spriteRenderer.setLastSize(id, buffersHolder, width, height);
        }

        return this;
    }

    @Override
    protected void updateLastRenderValues() {
        lastValuesUpdateConsumer.accept(this);
    }

    @Override
    public void postWorldUpdate() {
        updateRenderValues();
    }

    @Override
    protected void updateRenderValues() {
        if (alphaVelocity != 0) {
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

            spriteRenderer.setColorAlpha(id, buffersHolder, color.w);
        }
    }

    public void defaultUpdateLastValues() {
        spriteRenderer.setLastPosition(id, buffersHolder, particle.getX(), particle.getY());
        spriteRenderer.setLastRotation(id, buffersHolder, particle.getSin(), particle.getCos());

        if (particle.getSizeVelocity() != 0) {
            spriteRenderer.setLastSize(id, buffersHolder, particle.getSizeX(), particle.getSizeY());
        }

        if (alphaVelocity != 0) {
            spriteRenderer.setLastColorAlpha(id, buffersHolder, color.w);
        }
    }

    public void setPosition(float x, float y) {
        spriteRenderer.setPosition(id, buffersHolder, x, y);
    }

    public void setLastPosition() {
        spriteRenderer.setLastPosition(id, buffersHolder, particle.getX(), particle.getY());
    }

    public void setRotation(float sin, float cos) {
        spriteRenderer.setRotation(id, buffersHolder, sin, cos);
    }

    public void setLastRotation() {
        spriteRenderer.setLastRotation(id, buffersHolder, particle.getSin(), particle.getCos());
    }

    @Override
    public void setSize(float sizeX, float sizeY) {
        spriteRenderer.setSize(id, buffersHolder, sizeX, sizeY);
    }

    public void setLastSize() {
        spriteRenderer.setLastSize(id, buffersHolder, particle.getSizeX(), particle.getSizeY());
    }

    public void setColorAlpha(float a) {
        color.w = a;
        spriteRenderer.setColorAlpha(id, buffersHolder, a);
    }

    public void setLastColorAlpha() {
        spriteRenderer.setLastColorAlpha(id, buffersHolder, color.w);
    }

    void putToBuffer(int index) {
        int offset = index * AbstractSpriteRenderer.COMMAND_SIZE_IN_BYTES;
        buffersHolder.putCommandData(offset + AbstractSpriteRenderer.BASE_VERTEX_OFFSET,
                AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX);
        buffersHolder.putCommandData(offset + AbstractSpriteRenderer.BASE_INSTANCE_OFFSET, id);
    }
}
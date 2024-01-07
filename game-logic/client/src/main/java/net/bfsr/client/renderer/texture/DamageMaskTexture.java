package net.bfsr.client.renderer.texture;

import net.bfsr.engine.Engine;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.AbstractDamageMaskTexture;
import net.bfsr.engine.renderer.texture.AbstractTexture;

import java.nio.ByteBuffer;

public class DamageMaskTexture extends AbstractDamageMaskTexture {
    private final AbstractTexture texture;
    private final ByteBuffer byteBuffer;
    private float lastFireAmount, lastFireUVAnimation;
    private float fireAmount, fireUVAnimation;
    private boolean changeFire;
    private final float fireAnimationSpeed = Engine.convertToDeltaTime(0.24f);
    private final float uvAnimationSpeed = Engine.convertToDeltaTime(0.12f);

    public DamageMaskTexture(int width, int height, ByteBuffer byteBuffer) {
        this.texture = Engine.assetsManager.newTexture(width, height);
        this.byteBuffer = byteBuffer;
        this.lastFireAmount = fireAmount = 2.0f;
        this.lastFireUVAnimation = fireUVAnimation = (float) (Math.random() * MathUtils.TWO_PI);
    }

    public void createWhiteMask() {
        texture.create();

        for (int i = 0; i < texture.getWidth(); i++) {
            for (int j = 0; j < texture.getHeight(); j++) {
                byteBuffer.put(i * texture.getWidth() + j, (byte) 255);
            }
        }

        Engine.renderer.uploadTexture(texture, GL.GL_R8, GL.GL_RED, GL.GL_CLAMP_TO_EDGE, GL.GL_LINEAR, byteBuffer);
    }

    public void createEmpty() {
        texture.create();
        Engine.renderer.uploadEmpty(texture, GL.GL_R8, GL.GL_RED);
    }

    public void upload(int x, int y, int width, int height, ByteBuffer byteBuffer) {
        Engine.renderer.subImage2D(texture.getId(), x, y, width, height, GL.GL_RED, byteBuffer);
    }

    public void updateEffects() {
        lastFireAmount = fireAmount;
        lastFireUVAnimation = fireUVAnimation;

        if (changeFire) {
            fireAmount -= fireAnimationSpeed;
            if (fireAmount < 0.6f) {
                fireAmount = 0.6f;
                changeFire = false;
            }
        } else {
            fireAmount += fireAnimationSpeed;
            if (fireAmount > 1.75f) {
                fireAmount = 1.75f;
                changeFire = true;
            }
        }

        fireUVAnimation += uvAnimationSpeed;
        if (fireUVAnimation > MathUtils.TWO_PI) {
            fireUVAnimation -= MathUtils.TWO_PI;
        }
    }

    @Override
    public long getTextureHandle() {
        return texture.getTextureHandle();
    }

    @Override
    public float getFireAmount(float interpolation) {
        return lastFireAmount + (fireAmount - lastFireAmount) * interpolation;
    }

    @Override
    public float getFireUVAnimation(float interpolation) {
        return lastFireUVAnimation + (fireUVAnimation - lastFireUVAnimation) * interpolation;
    }

    @Override
    public void delete() {
        texture.delete();
    }

    public int getWidth() {
        return texture.getWidth();
    }
}
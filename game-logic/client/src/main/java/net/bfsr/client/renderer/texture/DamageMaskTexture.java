package net.bfsr.client.renderer.texture;

import net.bfsr.client.Core;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.AbstractDamageMaskTexture;
import net.bfsr.engine.renderer.texture.AbstractTexture;

import java.nio.ByteBuffer;

public class DamageMaskTexture extends AbstractDamageMaskTexture {
    private static final ByteBuffer WHITE_BUFFER = ByteBuffer.allocateDirect(4).put((byte) 255).flip();

    private final AbstractTexture texture;
    private float lastFireAmount, lastFireUVAnimation;
    private float fireAmount, fireUVAnimation;
    private boolean changeFire;
    private final float fireAnimationSpeed = Core.get().convertToDeltaTime(0.24f);
    private final float uvAnimationSpeed = Core.get().convertToDeltaTime(0.12f);

    public DamageMaskTexture(int width, int height) {
        this.texture = Engine.assetsManager.newTexture(width, height);
        this.lastFireAmount = fireAmount = 2.0f;
        this.lastFireUVAnimation = fireUVAnimation = (float) (Math.random() * MathUtils.TWO_PI);
    }

    public void createEmpty() {
        texture.create();
        Engine.renderer.fillTexture(texture, GL.GL_R8, GL.GL_RED, WHITE_BUFFER);
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
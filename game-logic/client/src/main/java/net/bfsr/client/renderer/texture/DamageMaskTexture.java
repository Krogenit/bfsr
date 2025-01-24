package net.bfsr.client.renderer.texture;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.AbstractDamageMaskTexture;
import net.bfsr.engine.renderer.texture.AbstractTexture;

import java.nio.ByteBuffer;

public class DamageMaskTexture extends AbstractDamageMaskTexture {
    public static final ByteBuffer WHITE_BUFFER = ByteBuffer.allocateDirect(4).put((byte) 255).flip();

    private final AbstractRenderer renderer = Engine.getRenderer();
    private final AbstractTexture texture;

    @Getter
    private float fireAmount, fireUVAnimation;
    private boolean changeFire;
    private final float fireAnimationSpeed;
    private final float uvAnimationSpeed;

    public DamageMaskTexture(int width, int height) {
        texture = Engine.getAssetsManager().newTexture(width, height);
        fireAmount = 2.0f;
        fireUVAnimation = (float) (Math.random() * MathUtils.TWO_PI);
        fireAnimationSpeed = Engine.convertToDeltaTime(0.24f);
        uvAnimationSpeed = Engine.convertToDeltaTime(0.12f);
    }

    public void createEmpty() {
        texture.create();
        renderer.uploadFilledTexture(texture, GL.GL_R8, GL.GL_RED, WHITE_BUFFER);
    }

    public void fillEmpty() {
        renderer.fullTexture(texture, GL.GL_R8, GL.GL_RED, WHITE_BUFFER);
    }

    public void upload(int x, int y, int width, int height, ByteBuffer byteBuffer) {
        renderer.subImage2D(texture.getId(), x, y, width, height, GL.GL_RED, byteBuffer);
    }

    public void updateEffects() {

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
    public void delete() {
        texture.delete();
    }

    public int getWidth() {
        return texture.getWidth();
    }
}
package net.bfsr.client.renderer.texture;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.math.MathUtils;
import net.bfsr.util.TimeUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

@Getter
@Setter
public class DamageMaskTexture extends Texture {
    private static final ByteBuffer BYTE_BUFFER = BufferUtils.createByteBuffer(128 << 7);

    public int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE;
    public int maxX, maxY;
    private final ByteBuffer byteBuffer;
    private float lastFireAmount, lastFireUVAnimation;
    private float fireAmount, fireUVAnimation;
    private boolean changeFire;

    public DamageMaskTexture(int width, int height, ByteBuffer byteBuffer) {
        super(width, height);
        this.byteBuffer = byteBuffer;
        this.lastFireAmount = fireAmount = 2.0f;
        this.lastFireUVAnimation = fireUVAnimation = (float) (Math.random() * MathUtils.TWO_PI);
    }

    public void createWhiteMask() {
        create();
        GL45C.glTextureStorage2D(id, 1, GL30C.GL_R8, width, height);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                byteBuffer.put(i * width + j, (byte) 255);
            }
        }

        GL45C.glTextureSubImage2D(id, 0, 0, 0, width, height, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, byteBuffer);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        textureHandle = ARBBindlessTexture.glGetTextureHandleARB(id);
        ARBBindlessTexture.glMakeTextureHandleResidentARB(textureHandle);
    }

    public void createEmpty() {
        create();
        GL45C.glTextureStorage2D(id, 1, GL30C.GL_R8, width, height);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL44C.glClearTexImage(id, 0, GL11C.GL_RED, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        textureHandle = ARBBindlessTexture.glGetTextureHandleARB(id);
        ARBBindlessTexture.glMakeTextureHandleResidentARB(textureHandle);
    }

    public void upload(int x, int y, int width, int height, ByteBuffer byteBuffer) {
        GL45C.glTextureSubImage2D(id, 0, x, y, width, height, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, byteBuffer);
    }

    public void upload() {
        if (maxX > 0 && maxY > 0) {
            int width = maxX - x + 1;
            int height = maxY - y + 1;
            int index = 0;

            for (int i = y; i <= maxY; i++, index += width) {
                BYTE_BUFFER.put(index, byteBuffer, i * this.height + x, width);
            }

            GL45C.glTextureSubImage2D(id, 0, x, y, width, height, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, BYTE_BUFFER);
        }
    }

    public void updateEffects() {
        float speed = 0.24f * TimeUtils.UPDATE_DELTA_TIME;
        float uvAnimationSpeed = 0.12f * TimeUtils.UPDATE_DELTA_TIME;

        lastFireAmount = fireAmount;
        lastFireUVAnimation = fireUVAnimation;

        if (changeFire) {
            fireAmount -= speed;
            if (fireAmount < 0.6f) {
                fireAmount = 0.6f;
                changeFire = false;
            }
        } else {
            fireAmount += speed;
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

    public float getFireAmount(float interpolation) {
        return lastFireAmount + (fireAmount - lastFireAmount) * interpolation;
    }

    public float getFireUVAnimation(float interpolation) {
        return lastFireUVAnimation + (fireUVAnimation - lastFireUVAnimation) * interpolation;
    }

    public void reset() {
        x = Integer.MAX_VALUE;
        y = Integer.MAX_VALUE;
        maxX = 0;
        maxY = 0;
    }

    public void copyFrom(ByteBuffer byteBuffer) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.byteBuffer.put(i * width + j, byteBuffer.get(i * width + j));
            }
        }
    }
}
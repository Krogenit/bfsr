package net.bfsr.util;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class MatrixBufferUtils {
    public static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);

    public static void setPosition(FloatBuffer matrixBuffer, float x, float y) {
        matrixBuffer.position(12);
        matrixBuffer.put(x);
        matrixBuffer.put(y);
        matrixBuffer.position(0);
    }

    public static void setX(FloatBuffer matrixBuffer, float x) {
        matrixBuffer.position(12);
        matrixBuffer.put(x);
        matrixBuffer.position(0);
    }

    public static void setY(FloatBuffer matrixBuffer, float y) {
        matrixBuffer.position(13);
        matrixBuffer.put(y);
        matrixBuffer.position(0);
    }

    public static void set(FloatBuffer matrixBuffer, float x, float y, float width, float height) {
        setPosition(matrixBuffer, x, y);
        matrixBuffer.put(width);
        setHeight(matrixBuffer, height);
    }

    public static void setSize(FloatBuffer matrixBuffer, float width, float height) {
        matrixBuffer.position(0);
        matrixBuffer.put(width);
        matrixBuffer.position(5);
        matrixBuffer.put(height);
        matrixBuffer.position(0);
    }

    public static void setWidth(FloatBuffer matrixBuffer, float width) {
        matrixBuffer.position(0);
        matrixBuffer.put(width);
        matrixBuffer.position(0);
    }

    public static void setHeight(FloatBuffer matrixBuffer, float height) {
        matrixBuffer.position(5);
        matrixBuffer.put(height);
        matrixBuffer.position(0);
    }

    public static float getY(FloatBuffer matrixBuffer) {
        return matrixBuffer.get(13);
    }
}

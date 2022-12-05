package net.bfsr.math;

import org.joml.Matrix4f;

import java.nio.FloatBuffer;

public class MatrixUtils {
    public static void translateIdentity(Matrix4f matrix, float x, float y) {
        matrix.set(3, 0, x);
        matrix.set(3, 1, y);
    }

    public static void rotateAboutZIdentity(Matrix4f matrix, float angle) {
        float cos = LUT.cos(angle);
        float sin = LUT.sin(angle);
        float minusSin = -sin;

        matrix.set(0, 0, cos);//m00
        matrix.set(0, 1, sin);//m01
        matrix.set(1, 0, minusSin);//m10
        matrix.set(1, 1, cos);//m11
    }

    public static void translateIdentity(FloatBuffer matrixBuffer, float x, float y) {
        matrixBuffer.position(12);
        matrixBuffer.put(x);//m30
        matrixBuffer.put(y);//m31
        matrixBuffer.position(0);
    }

    public static void rotateAboutZIdentity(FloatBuffer matrixBuffer, float angle) {
        float cos = LUT.cos(angle);
        float sin = LUT.sin(angle);
        float minusSin = -sin;

        matrixBuffer.position(0);
        matrixBuffer.put(cos);//m00
        matrixBuffer.put(sin);//m01
        matrixBuffer.position(4);
        matrixBuffer.put(minusSin);//m10
        matrixBuffer.put(cos);//m11
        matrixBuffer.position(0);
    }
}

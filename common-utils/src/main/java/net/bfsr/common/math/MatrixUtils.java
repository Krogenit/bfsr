package net.bfsr.common.math;

import org.joml.Matrix4f;

public final class MatrixUtils {
    public static void translateIdentity(Matrix4f matrix, float x, float y) {
        matrix.m30(x);
        matrix.m31(y);
    }

    public static void translate(Matrix4f matrix, float x, float y) {
        matrix.m30(matrix.m30() + matrix.m00() * x + matrix.m10() * y);
        matrix.m31(matrix.m31() + matrix.m01() * x + matrix.m11() * y);
        matrix.m32(matrix.m32() + matrix.m02() * x + matrix.m12() * y);
        matrix.m33(matrix.m33() + matrix.m03() * x + matrix.m13() * y);
    }

    public static void rotateAboutZ(Matrix4f matrix, float angle) {
        float cos = LUT.cos(angle);
        float sin = LUT.sin(angle);
        float minusSin = -sin;

        float t00 = matrix.m00() * cos + matrix.m10() * sin;
        float t01 = matrix.m01() * cos + matrix.m11() * sin;
        float t02 = matrix.m02() * cos + matrix.m12() * sin;
        float t03 = matrix.m03() * cos + matrix.m13() * sin;
        float t10 = matrix.m00() * minusSin + matrix.m10() * cos;
        float t11 = matrix.m01() * minusSin + matrix.m11() * cos;
        float t12 = matrix.m02() * minusSin + matrix.m12() * cos;
        float t13 = matrix.m03() * minusSin + matrix.m13() * cos;
        matrix.m00(t00);
        matrix.m01(t01);
        matrix.m02(t02);
        matrix.m03(t03);
        matrix.m10(t10);
        matrix.m11(t11);
        matrix.m12(t12);
        matrix.m13(t13);
    }

    public static void scale(Matrix4f matrix, float x, float y) {
        matrix.m00(matrix.m00() * x);
        matrix.m01(matrix.m01() * x);
        matrix.m02(matrix.m02() * x);
        matrix.m03(matrix.m03() * x);
        matrix.m10(matrix.m10() * y);
        matrix.m11(matrix.m11() * y);
        matrix.m12(matrix.m12() * y);
        matrix.m13(matrix.m13() * y);
    }
}
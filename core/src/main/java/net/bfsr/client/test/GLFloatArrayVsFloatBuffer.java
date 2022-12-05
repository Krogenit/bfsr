package net.bfsr.client.test;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;

import java.nio.FloatBuffer;

public class GLFloatArrayVsFloatBuffer {
    //buffer better
    public static void test() {
        int buffer = GL45.glCreateBuffers();
        int size = 1000 * 16;
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(size);
        float[] floats = new float[size];
        for (int i = 0; i < size; i++) {
            floatBuffer.put(i * 5);
            floats[i] = i * 6;
        }
        floatBuffer.flip();
        GL45.glNamedBufferData(buffer, floatBuffer, GL15.GL_DYNAMIC_DRAW);
        long startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            GL45.glNamedBufferSubData(buffer, 0, floatBuffer);
        }
        float result1 = (System.nanoTime() - startTime) / 1_000_000.0f;
        System.out.println("floatBuffer " + result1 + " ms");
        startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            GL45.glNamedBufferSubData(buffer, 0, floats);
        }
        float result2 = (System.nanoTime() - startTime) / 1_000_000.0f;
        System.out.println("floatArray " + result2 + " ms");
        if (result1 > result2) {
            System.out.println("floatArray better than floatBuffer");
        } else {
            System.out.println("floatBuffer better than floatArray");
        }
        GL15.glDeleteBuffers(buffer);
    }
}

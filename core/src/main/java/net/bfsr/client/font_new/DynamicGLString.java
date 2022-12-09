package net.bfsr.client.font_new;

import org.lwjgl.opengl.GL44;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;

public class DynamicGLString extends GLString {
    @Override
    public void fillBuffer(FloatBuffer vertexBuffer, LongBuffer textureBuffer) {
        fillBuffer(vertexBuffer, textureBuffer, GL44.GL_DYNAMIC_STORAGE_BIT);
    }
}

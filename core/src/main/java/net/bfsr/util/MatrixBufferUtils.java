package net.bfsr.util;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public final class MatrixBufferUtils {
    public static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
}

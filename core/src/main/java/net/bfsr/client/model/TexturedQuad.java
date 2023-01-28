package net.bfsr.client.model;

import org.lwjgl.system.MemoryStack;

public final class TexturedQuad extends Quad {
    public static TexturedQuad createQuad() {
        return new TexturedQuad(new float[]{
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f,
        });
    }

    public static TexturedQuad createCenteredQuad() {
        return new TexturedQuad(new float[]{
                -0.5f, 0.5f, 0.0f, 1.0f,
                0.5f, 0.5f, 1.0f, 1.0f,
                0.5f, -0.5f, 1.0f, 0.0f,
                -0.5f, -0.5f, 0.0f, 0.0f
        });
    }

    public static TexturedQuad createCounterClockWiseCenteredQuad() {
        return new TexturedQuad(new float[]{
                0.5f, -0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, 1.0f, 1.0f,
                -0.5f, 0.5f, 0.0f, 1.0f,
                -0.5f, -0.5f, 0.0f, 0.0f
        });
    }

    public static TexturedQuad createParticleCenteredQuad() {
        TexturedQuad texturedQuad = new TexturedQuad(new float[]{
                -0.5f, 0.5f, 0.0f, 1.0f,
                0.5f, 0.5f, 1.0f, 1.0f,
                0.5f, -0.5f, 1.0f, 0.0f,
                -0.5f, -0.5f, 0.0f, 0.0f
        }, 3);
        texturedQuad.vao.enableAttributes(7);
        return texturedQuad;
    }

    private TexturedQuad(float[] positionsUvs) {
        this(positionsUvs, 2);
    }

    public TexturedQuad(float[] positionsUvs, int vboCount) {
        super(vboCount);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            vao.createVertexBuffers();
            vao.attributeBindingAndFormat(0, 4, 0, 0);
            vao.updateVertexBuffer(0, stack.mallocFloat(positionsUvs.length).put(positionsUvs).flip(), 0, 16);
            vao.updateIndexBuffer(1, stack.mallocInt(INDICES.length).put(INDICES).flip(), 0);
            vao.enableAttributes(1);
        }
    }
}

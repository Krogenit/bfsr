package net.bfsr.client.shader.primitive;

import lombok.Getter;

@Getter
public class PrimitiveShaders {
    public static final PrimitiveShaders INSTANCE = new PrimitiveShaders();

    private final VertexColorShader vertexColorShader = new VertexColorShader();
    private final VertexColorTextureShader vertexColorTextureShader = new VertexColorTextureShader();
}

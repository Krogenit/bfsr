package net.bfsr.client.font;

@Deprecated
public class TextMeshDataOptimized {

    private final float[] vertexPositionsAndTextureCoords;

    protected TextMeshDataOptimized(float[] vertexPositionsAndTextureCoords) {
        this.vertexPositionsAndTextureCoords = vertexPositionsAndTextureCoords;
    }

    public float[] getVertexPositionsAndTextureCoords() {
        return vertexPositionsAndTextureCoords;
    }

    public int getVertexCount() {
        return vertexPositionsAndTextureCoords.length / 4;
    }
}

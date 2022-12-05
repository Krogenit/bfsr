package net.bfsr.client.font;

/**
 * Stores the vertex data for all the quads on which a text will be rendered.
 *
 * @author Karl
 */
@Deprecated
public class TextMeshData {

    private final float[] vertexPositions;
    private final float[] textureCoords;

    protected TextMeshData(float[] vertexPositions, float[] textureCoords) {
        this.vertexPositions = vertexPositions;
        this.textureCoords = textureCoords;
    }

    public float[] getVertexPositions() {
        return vertexPositions;
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public int getVertexCount() {
        return vertexPositions.length / 2;
    }

}

package net.bfsr.client.font;

import lombok.Getter;

import java.io.File;

/**
 * Represents a font. It holds the font's texture atlas as well as having the
 * ability to create the quad vertices for any text using this font.
 *
 * @author Karl
 */
@Deprecated
public class FontType {

    private final int textureAtlas;
    private final TextMeshCreator loader;
    @Getter
    private final float lineHeight;

    /**
     * Creates a new font and loads up the data about each character from the
     * font file.
     *
     * @param textureAtlas - the ID of the font atlas texture.
     * @param fontFile     - the font file containing information about each character in
     *                     the texture atlas.
     */
    public FontType(int textureAtlas, File fontFile) {
        this.textureAtlas = textureAtlas;
        this.loader = new TextMeshCreator(fontFile);
        this.lineHeight = loader.calculateFontHeight();
    }

    /**
     * @return The font texture atlas.
     */
    public int getTextureAtlas() {
        return textureAtlas;
    }

    /**
     * Takes in an unloaded text and calculate all of the vertices for the quads
     * on which this text will be rendered. The vertex positions and texture
     * coords and calculated based on the information from the font file.
     *
     * @param text - the unloaded text.
     * @return Information about the vertices of all the quads.
     */
    public TextMeshData loadText(GUIText text) {
        return loader.createTextMesh(text);
    }

    public float[] loadTextOptimized(String text, float fontSizeX, float fontSizeY, float maxLineWidth, boolean isCentered) {
        return loader.createTextMeshOptimized(text, fontSizeX, fontSizeY, maxLineWidth, isCentered, this.lineHeight);
    }

    public TextMeshCreator getLoader() {
        return loader;
    }
}

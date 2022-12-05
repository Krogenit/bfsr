package net.bfsr.client.font;

/**
 * Simple data structure class holding information about a certain glyph in the
 * font texture atlas. All sizes are for a font-size of 1.
 *
 * @author Karl
 */
@Deprecated
public class Character {

    private final int id;
    private final float xTextureCoord;
    private final float yTextureCoord;
    private final float xMaxTextureCoord;
    private final float yMaxTextureCoord;
    private final float xOffset;
    private final float yOffset;
    private final float sizeX;
    private final float sizeY;
    private final float xAdvance;

    /**
     * @param id            - the ASCII value of the character.
     * @param xTextureCoord - the x texture coordinate for the top left corner of the
     *                      character in the texture atlas.
     * @param yTextureCoord - the y texture coordinate for the top left corner of the
     *                      character in the texture atlas.
     * @param xTexSize      - the width of the character in the texture atlas.
     * @param yTexSize      - the height of the character in the texture atlas.
     * @param xOffset       - the x distance from the curser to the left edge of the
     *                      character's quad.
     * @param yOffset       - the y distance from the curser to the top edge of the
     *                      character's quad.
     * @param sizeX         - the width of the character's quad in screen space.
     * @param sizeY         - the height of the character's quad in screen space.
     * @param xAdvance      - how far in pixels the cursor should advance after adding
     *                      this character.
     */
    protected Character(int id, float xTextureCoord, float yTextureCoord, float xTexSize, float yTexSize,
                        float xOffset, float yOffset, float sizeX, float sizeY, float xAdvance) {
        this.id = id;
        this.xTextureCoord = xTextureCoord;
        this.yTextureCoord = yTextureCoord;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.xMaxTextureCoord = xTexSize + xTextureCoord;
        this.yMaxTextureCoord = yTexSize + yTextureCoord;
        this.xAdvance = xAdvance;
    }

    protected int getId() {
        return id;
    }

    protected float getXTextureCoord() {
        return xTextureCoord;
    }

    protected float getYTextureCoord() {
        return yTextureCoord;
    }

    protected float getXMaxTextureCoord() {
        return xMaxTextureCoord;
    }

    protected float getYMaxTextureCoord() {
        return yMaxTextureCoord;
    }

    protected float getXOffset() {
        return xOffset;
    }

    protected float getYOffset() {
        return yOffset;
    }

    protected float getSizeX() {
        return sizeX;
    }

    protected float getSizeY() {
        return sizeY;
    }

    protected float getXAdvance() {
        return xAdvance;
    }

}

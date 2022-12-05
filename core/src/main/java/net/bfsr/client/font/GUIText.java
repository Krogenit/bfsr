package net.bfsr.client.font;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.math.EnumZoomFactor;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Deprecated
public class GUIText {

    @Getter
    @Setter
    private String textString;
    @Getter
    @Setter
    private Vector2f fontSize;

    @Getter
    private int textMeshVao;
    @Getter
    @Setter
    private int vertexCount;
    private final Vector4f color;

    private Vector2f position;
    @Getter
    private float maxLineSize;
    @Setter
    private int numberOfLines;

    @Getter
    private final FontType font;

    @Getter
    private final boolean centered;
    @Getter
    @Setter
    private EnumZoomFactor zoomFactor = EnumZoomFactor.Gui;
    @Getter
    private EnumParticlePositionType positionType;
    @Getter
    @Setter
    private float lineWidth;
    @Getter
    @Setter
    private boolean removed;
    @Getter
    private boolean shadow = true;
    @Getter
    private float lineHeight = 1.0f;

    public GUIText(String text, Vector3f fontSize, FontType font, Vector2f position, Vector4f color, float maxLineLength, boolean centered, EnumParticlePositionType positionType) {
        this.textString = text;
        this.fontSize = new Vector2f(fontSize.x, fontSize.y);
        this.font = font;
        this.position = position;
        this.maxLineSize = maxLineLength;
        this.centered = centered;
        this.color = color;
        this.positionType = positionType;
        this.lineHeight = fontSize.z;
        FontRenderer.loadText(this);
    }

    public GUIText(String text, Vector2f fontSize, FontType font, Vector2f position, Vector4f color, float maxLineLength, boolean centered, EnumParticlePositionType positionType) {
        this.textString = text;
        this.fontSize = fontSize;
        this.font = font;
        this.position = position;
        this.maxLineSize = maxLineLength;
        this.centered = centered;
        this.color = color;
        this.positionType = positionType;
        FontRenderer.loadText(this);
    }

    public GUIText(String text, Vector2f fontSize, Vector2f position, Vector4f color, float maxLineLength, boolean centered, EnumParticlePositionType positionType) {
        this(text, fontSize, FontRegistry.XOLONIUM, position, color, maxLineLength, centered, positionType);
    }

    public GUIText(String text, Vector3f fontSize, FontType font, Vector2f position, Vector4f color, boolean centered, EnumParticlePositionType positionType) {
        this(text, fontSize, font, position, color, 1.0f, centered, positionType);
    }

    public GUIText(String text, Vector2f fontSize, FontType font, Vector2f position, Vector4f color, boolean centered, EnumParticlePositionType positionType) {
        this(text, fontSize, font, position, color, 1.0f, centered, positionType);
    }

    public GUIText(String text, Vector2f fontSize, Vector2f position, Vector4f color, boolean centered, EnumParticlePositionType positionType) {
        this(text, fontSize, FontRegistry.XOLONIUM, position, color, 1.0f, centered, positionType);
    }

    public GUIText(String text, Vector2f position, Vector4f color, EnumParticlePositionType positionType) {
        this(text, new Vector2f(1f, 1f), FontRegistry.XOLONIUM, position, color, 1.0f, true, positionType);
    }

    public void clear() {
        FontRenderer.removeText(this);
    }

    public void updateText(String text) {
        this.textString = text;
        if (removed) FontRenderer.loadText(this);
        else FontRenderer.updateText(this);
    }

    public void setFontSize(float x, float y) {
        this.fontSize.x = x;
        this.fontSize.y = y;
    }

    public void setColor(float r, float g, float b, float alpha) {
        color.set(r, g, b, alpha);
    }

    public void setColor(float r, float g, float b) {
        color.set(r, g, b, 1f);
    }

    public Vector4f getColor() {
        return color;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        this.position.x = x;
        this.position.y = y;
    }

    public Vector2f getPosition() {
        return position;
    }

    public int getMesh() {
        return textMeshVao;
    }

    public void setMeshInfo(int vao, int verticesCount) {
        this.textMeshVao = vao;
        this.vertexCount = verticesCount;
    }
}

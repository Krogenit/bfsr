package ru.krogenit.bfsr.client.font;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.math.EnumZoomFactor;

public class GUIText {

	private String textString;
	private Vector2f fontSize;

	private int textMeshVao;
	private int vertexCount;
	private final Vector4f color;

	private Vector2f position;
	private float lineMaxSize;
	private int numberOfLines;

	private final FontType font;

	private final boolean centerText;
	private EnumZoomFactor zoomFactor = EnumZoomFactor.Gui;
	private EnumParticlePositionType positionType;
	private float lineWidth;
	private boolean removed;
	private boolean shadow = true;
	private float lineHeight = 1.0f;

	public GUIText(String text, Vector3f fontSize, FontType font, Vector2f position, Vector4f color, float maxLineLength, boolean centered, EnumParticlePositionType positionType) {
		this.textString = text;
		this.fontSize = new Vector2f(fontSize.x, fontSize.y);
		this.font = font;
		this.position = position;
		this.lineMaxSize = maxLineLength;
		this.centerText = centered;
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
		this.lineMaxSize = maxLineLength;
		this.centerText = centered;
		this.color = color;
		this.positionType = positionType;
		FontRenderer.loadText(this);
	}

	public GUIText(String text, Vector2f fontSize, Vector2f position, Vector4f color, float maxLineLength, boolean centered, EnumParticlePositionType positionType) {
		this(text, fontSize, FontRenderer.XOLONIUM, position, color, maxLineLength, centered, positionType);
	}

	public GUIText(String text, Vector3f fontSize, FontType font, Vector2f position, Vector4f color, boolean centered, EnumParticlePositionType positionType) {
		this(text, fontSize, font, position, color, 1.0f, centered, positionType);
	}

	public GUIText(String text, Vector2f fontSize, FontType font, Vector2f position, Vector4f color, boolean centered, EnumParticlePositionType positionType) {
		this(text, fontSize, font, position, color, 1.0f, centered, positionType);
	}

	public GUIText(String text, Vector2f fontSize, Vector2f position, Vector4f color, boolean centered, EnumParticlePositionType positionType) {
		this(text, fontSize, FontRenderer.XOLONIUM, position, color, centered, positionType);
	}

	public GUIText(String text, Vector2f position, Vector4f color, EnumParticlePositionType positionType) {
		this(text, new Vector2f(1f, 1f), position, color, true, positionType);
	}

	public void clear() {
		FontRenderer.removeText(this);
	}
	
	public void updateText(String text) {
		this.textString = text;
		if(removed) FontRenderer.loadText(this);
		else FontRenderer.updateText(this);
	}

	public FontType getFont() {
		return font;
	}
	
	public void setFontSize(Vector2f fontSize) {
		this.fontSize = fontSize;
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

	public int getNumberOfLines() {
		return numberOfLines;
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

	public int getVertexCount() {
		return this.vertexCount;
	}

	public Vector2f getFontSize() {
		return fontSize;
	}

	protected void setNumberOfLines(int number) {
		this.numberOfLines = number;
	}

	protected boolean isCentered() {
		return centerText;
	}

	protected float getMaxLineSize() {
		return lineMaxSize;
	}

	public String getTextString() {
		return textString;
	}

	public EnumZoomFactor getZoomFactor() {
		return zoomFactor;
	}

	public void setZoomFactor(EnumZoomFactor zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

	public int getTextMeshVao() {
		return textMeshVao;
	}

	public void setTextMeshVao(int textMeshVao) {
		this.textMeshVao = textMeshVao;
	}

	public EnumParticlePositionType getPositionType() {
		return positionType;
	}

	public void setPositionType(EnumParticlePositionType positionType) {
		this.positionType = positionType;
	}

	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
	}

	public float getLineWidth() {
		return lineWidth;
	}

	public void setRemoved(boolean removed) {
		this.removed = removed;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setShadow(boolean shadow) {
		this.shadow = shadow;
	}

	public boolean isShadow() {
		return shadow;
	}

	public float getLineHeight() {
		return lineHeight;
	}

	public void setLineHeight(float lineHeight) {
		this.lineHeight = lineHeight;
	}

	public void setVertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}
	
	public void setTextString(String textString) {
		this.textString = textString;
	}
	
	public void setLineMaxSize(float lineMaxSize) {
		this.lineMaxSize = lineMaxSize;
	}
}

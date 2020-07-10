package ru.krogenit.bfsr.client.shader;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class ParticleShader extends BaseShader {

	private int loc_animatedTexture;
	private int loc_texCoordInfo;
	private int loc_texOffset;

	public ParticleShader() {
		super("particle");
	}

	@Override
	protected void getAllUniformLocations() {
		super.getAllUniformLocations();

		loc_animatedTexture = super.getUniformLocation("animatedTexture");
		loc_texCoordInfo = super.getUniformLocation("texCoordInfo");
		loc_texOffset = super.getUniformLocation("texOffset");
	}

	@Override
	protected void init() {
		setTextureOpaqueId(0);
		setColor(new Vector4f(1, 1, 1, 1));
	}

	public void setTextureCoordInfo(Vector2f offset1, Vector2f offset2, float numRows, float blend) {
		this.setVector(loc_texOffset, offset1.x, offset1.y, offset2.x, offset2.y);
		this.setVector(loc_texCoordInfo, new Vector2f(numRows, blend));

	}

	private boolean prevAnimatedTexture;

	public void setAnimatedTexture(boolean value) {
		if (prevAnimatedTexture != value) {
			this.setBoolean(loc_animatedTexture, value);
			prevAnimatedTexture = value;
		}
	}

	public void setTexCoordInfo(Vector2f value) {
		this.setVector(loc_texCoordInfo, value);
	}

	public void setOrthoMatrix(Matrix4f matrix) {
		this.setMat4(loc_orthoMat, matrix);
	}

	public void setModelViewMatrix(Matrix4f matrix) {
		this.setMat4(loc_modelViewMat, matrix);
	}

	public void setTextureOpaqueId(int id) {
		this.setInt(loc_textureOpaque, id);
	}

	public void enableTexture() {
		if (!useTexture) {
			this.setBoolean(loc_useTexture, true);
			useTexture = true;
		}
	}

	public void disableTexture() {
		if (useTexture) {
			this.setBoolean(loc_useTexture, false);
			useTexture = false;
		}
	}

	public void setColor(Vector4f color) {
		this.setVector(loc_color, color);
	}
}

package ru.krogenit.bfsr.client.shader;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class BaseShader extends ShaderProgram {
	protected int loc_useTexture;
	protected int loc_textureOpaque;
	protected int loc_color;
	protected int loc_orthoMat;
	protected int loc_modelViewMat;
	private int loc_uv_scale;
	private int loc_uv_offset;

	protected boolean useTexture;
	
	public BaseShader(String shaderName) {
		super(shaderName);
	}


	public BaseShader() {
		super("base");
	}

	@Override
	protected void getAllUniformLocations() {
		loc_color = super.getUniformLocation("color");
		loc_useTexture = super.getUniformLocation("useTexture");
		loc_textureOpaque = super.getUniformLocation("textureOpaque");

		loc_orthoMat = super.getUniformLocation("orthoMat");
		loc_modelViewMat = super.getUniformLocation("modelViewMat");
		
		loc_uv_scale = super.getUniformLocation("uv_scale");
		loc_uv_offset = super.getUniformLocation("uv_offset");
	}

	@Override
	protected void init() {
		setTextureOpaqueId(0);
		setColor(new Vector4f(1, 1, 1, 1));
		setUVScale(1f, 1f);
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
	
	public void setUVScale(float x, float y) {
		this.setVector(loc_uv_scale, x, y);
	}
	
	public void setUVOffset(float x, float y) {
		this.setVector(loc_uv_offset, x, y);
	}
}

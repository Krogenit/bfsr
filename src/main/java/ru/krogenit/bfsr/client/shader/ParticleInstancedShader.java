package ru.krogenit.bfsr.client.shader;

import org.joml.Matrix4f;

public class ParticleInstancedShader extends ShaderProgram {
	
	private int loc_useTexture;
	private int loc_textureOpaque;
	private int loc_orthoMat;
	private int loc_modelViewMat;

	private boolean useTexture;
	
	private int loc_blend;
	private int loc_animatedTexture;
	private int loc_numberOfRows;

	public ParticleInstancedShader() {
		super("particleInstanced");
	}

	@Override
	protected void getAllUniformLocations() {
		loc_useTexture = super.getUniformLocation("useTexture");
		loc_textureOpaque = super.getUniformLocation("textureOpaque");

		loc_orthoMat = super.getUniformLocation("orthoMat");
		loc_modelViewMat = super.getUniformLocation("modelViewMat");
		
		loc_animatedTexture = super.getUniformLocation("animatedTexture");
		loc_numberOfRows = super.getUniformLocation("numberOfRows");
	}
	
	@Override
	protected void init() {
		setTextureOpaqueId(0);
	}
	
	public void setNumberOfRows(float value) {
		this.setFloat(loc_numberOfRows, value);
	}

	public void setBlend(float value) {
		this.setFloat(loc_blend, value);
	}
	
	private boolean prevAnimatedTexture;
	
	public void setAnimatedTexture(boolean value) {
		if(prevAnimatedTexture != value) {
			this.setBoolean(loc_animatedTexture, value);
			prevAnimatedTexture = value;
		}
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
		if(!useTexture) {
			this.setBoolean(loc_useTexture, true);
			useTexture = true;
		}
	}
	
	public void disableTexture() {
		if(useTexture) {
			this.setBoolean(loc_useTexture, false);
			useTexture = false;
		}
	}
}

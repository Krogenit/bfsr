package ru.krogenit.bfsr.client.shader;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import ru.krogenit.bfsr.core.Core;

public class FontShader extends ShaderProgram {

	private int location_color;
	private int location_translation;
	private int loc_orthoMat;
	private int loc_modelViewMat;

	public FontShader() {
		super("font");
	}

	@Override
	protected void getAllUniformLocations() {
		location_color = super.getUniformLocation("color");
		location_translation = super.getUniformLocation("translation");
		loc_orthoMat = super.getUniformLocation("orthoMat");
		loc_modelViewMat = super.getUniformLocation("modelViewMat");
	}
	
	@Override
	protected void init() {
		setColor(new Vector4f(1f,1f,1f,1f));
	}

	public void setColor(Vector4f colour) {
		super.setVector(location_color, colour);
	}

	public void loadTranslation(Vector2f translation) {
		super.setVector(location_translation, translation);
	}

	public void setOrthoMatrix(Matrix4f matrix) {
		super.setMat4(loc_orthoMat, matrix);
	}

	public void setModelViewMatrix(Matrix4f matrix) {
		this.setMat4(loc_modelViewMat, matrix);
	}
}

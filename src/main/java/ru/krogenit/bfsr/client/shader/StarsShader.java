package ru.krogenit.bfsr.client.shader;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class StarsShader extends ShaderProgram {

	int loc_textureOpaque;
	int loc_coreColor, loc_haloColor;
	int loc_center, loc_resolution;
	int loc_coreRadius, loc_haloFalloff, loc_scale;
	
	public StarsShader() {
		super("stars");
	}
	
	@Override
	protected void getAllUniformLocations() {
		loc_coreColor = super.getUniformLocation("coreColor");
		loc_haloColor = super.getUniformLocation("haloColor");
		
		loc_textureOpaque = super.getUniformLocation("textureOpaque");
		
		loc_center = super.getUniformLocation("center");
		loc_resolution = super.getUniformLocation("resolution");
		
		loc_coreRadius = super.getUniformLocation("coreRadius");
		loc_haloFalloff = super.getUniformLocation("haloFalloff");
		loc_scale = super.getUniformLocation("scale");
	}

	@Override
	protected void init() {
		setTextureOpaqueId(0);
	}

	public void setTextureOpaqueId(int id) {
		this.setInt(loc_textureOpaque, id);
	}
	
	public void setCoreColor(Vector3f color) {
		this.setVector(loc_coreColor, color);
	}
	
	public void sethaloColor(Vector3f value) {
		this.setVector(loc_haloColor, value);
	}
	
	public void setScale(float value) {
		this.setFloat(loc_scale, value);
	}
	
	public void setCenter(Vector2f value) {
		this.setVector(loc_center, value);
	}
	
	public void setResolution(Vector2f value) {
		this.setVector(loc_resolution, value);
	}
	
	public void setFalloff(float value) {
		this.setFloat(loc_haloFalloff, value);
	}
	
	public void setCoreRadius(float value) {
		this.setFloat(loc_coreRadius, value);
	}
}

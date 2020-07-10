package ru.krogenit.bfsr.client.shader;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class NebulaShader extends ShaderProgram {

	private int loc_textureOpaque, loc_textureNoise;
	private int loc_color;
	private int loc_offset;
	private int loc_scale;
	private int loc_density;
	private int loc_falloff;
	private int loc_textureNoiseSize;
	private int loc_noiseType;
	private int loc_pNoiseRepeatVector;

	public NebulaShader() {
		super("nebula");
	}
	
	@Override
	protected void getAllUniformLocations() {
		loc_color = super.getUniformLocation("color");
		loc_textureOpaque = super.getUniformLocation("textureOpaque");
		loc_offset = super.getUniformLocation("offset");
		loc_scale = super.getUniformLocation("scale");
		loc_density = super.getUniformLocation("density");
		loc_falloff = super.getUniformLocation("falloff");
		loc_textureNoise = super.getUniformLocation("textureNoise");
		loc_textureNoiseSize = super.getUniformLocation("textureNoiseSize");
		loc_noiseType = super.getUniformLocation("noiseType");
		loc_pNoiseRepeatVector = super.getUniformLocation("pNoiseRepeatVector");
	}

	@Override
	protected void init() {
		setTextureOpaqueId(0);
		setNoiseTextureId(1);
	}
	
	public void setPNoiseRepeatVector(Vector4f value) {
		this.setVector(loc_pNoiseRepeatVector, value);
	}
	
	public void setNoiseType(int id) {
		this.setInt(loc_noiseType, id);
	}

	public void setTextureOpaqueId(int id) {
		this.setInt(loc_textureOpaque, id);
	}
	
	public void setNoiseTextureId(int id) {
		this.setInt(loc_textureNoise, id);
	}
	
	public void setColor(Vector3f color) {
		this.setVector(loc_color, color);
	}
	
	public void setOffset(Vector2f value) {
		this.setVector(loc_offset, value);
	}
	
	public void setScale(float value) {
		this.setFloat(loc_scale, value);
	}
	
	public void setDensity(float value) {
		this.setFloat(loc_density, value);
	}
	
	public void setFalloff(float value) {
		this.setFloat(loc_falloff, value);
	}
	
	public void setNoiseSize(float value) {
		this.setFloat(loc_textureNoiseSize, value);
	}
}

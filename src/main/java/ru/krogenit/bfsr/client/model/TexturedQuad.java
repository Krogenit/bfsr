package ru.krogenit.bfsr.client.model;

public class TexturedQuad extends Quad {
	
	protected static float[] text = new float[] { 
			0f, 0f, 
			0f, 1f, 
			1f, 1f, 
			1f, 0f };

	public TexturedQuad() {
		super(text);
	}
}

package ru.krogenit.bfsr.client.texture;

import static org.lwjgl.opengl.GL11.*;

import ru.krogenit.bfsr.client.render.OpenGLHelper;

public class Texture {
	private final int width;
	private final int height;
	private final int id;
	private int numberOfRows;
	
	public Texture(int width, int height, int id, int numberOfRows) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.numberOfRows = numberOfRows;
	}
	
	public Texture(int width, int height, int id) {
		this.id = id;
		this.width = width;
		this.height = height;
	}

	public Texture(int width, int height) {
		this.id = glGenTextures();
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getId() {
		return id;
	}

	public void delete() {
		glDeleteTextures(id);
	}
	
	public void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}
	
	public int getNumberOfRows() {
		return numberOfRows;
	}
	
	public void bind() {
		OpenGLHelper.bindTexture(id);
	}
}

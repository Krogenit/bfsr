package ru.krogenit.bfsr.client.render;

import ru.krogenit.bfsr.client.texture.Texture;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;

public class FrameBuffer {

	private int buffer;
	private int renderBuffer;
	private Texture[] texture;
	
	public FrameBuffer() {
		
	}
	
	public void generate() {
		buffer = glGenFramebuffers();
	}
	
	public void bind() {
		OpenGLHelper.bindFrameBuffer(buffer);
	}
	
	public void generateTexture(int count, int width, int height) {
		bind();
		texture = new Texture[count];
		int[] drawBuffers = new int[count];
		for(int i=0;i<count;i++) {
			texture[i] = getTexture(i, width, height);
			drawBuffers[i] = GL_COLOR_ATTACHMENT0 + i;
		}
		glDrawBuffers(drawBuffers);
		OpenGLHelper.bindFrameBuffer(0);
	}
	
	public void generateTexture(int width, int height) {
		bind();
		texture = new Texture[1];
		texture[0] = getTexture(0, width, height);
		int[] drawBuffers = new int[] {GL_COLOR_ATTACHMENT0};
		glDrawBuffers(drawBuffers);
		OpenGLHelper.bindFrameBuffer(0);
	}
	
	private Texture getTexture(int i, int width, int height) {
		Texture texture = new Texture(width, height);
		glBindTexture(GL_TEXTURE_2D, texture.getId());
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
		
//		float[] borderColor = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
//	    FloatBuffer buf = BufferUtils.createFloatBuffer(4);
//	    buf.put(borderColor);
//	    buf.flip();
//	    glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, buf);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, texture.getId(), 0);
		return texture;
	}
	
	public void generateRenderBuffer() {
		bind();
		renderBuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, texture[0].getWidth(), texture[0].getHeight());
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);
		OpenGLHelper.bindFrameBuffer(0);
	}
	
	public void bindTexture() {
		OpenGLHelper.bindTexture(texture[0].getId());
	}
	
	public Texture getTexture() {
		return texture[0];
	}
	
	public Texture getTexture(int i) {
		return texture[i];
	}
	
	public void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void viewPort(int width, int height) {
		glViewport(0, 0, width, height);
	}
	
	public void deleteTexture(int i) {
		texture[i].delete();
	}
	
	public void deleteTextures() {
		for(Texture texture : texture)
			texture.delete();
		texture = null;
	}

	public void delete() {
		glDeleteRenderbuffers(renderBuffer);
		glDeleteFramebuffers(buffer);
	}
	
	public void drawBuffer(int i) {
		glDrawBuffer(GL_COLOR_ATTACHMENT0 + i);
	}
	
	public int getBuffer() {
		return buffer;
	}
	
	public int getRenderBuffer() {
		return renderBuffer;
	}
	
}

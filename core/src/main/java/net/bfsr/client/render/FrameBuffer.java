package net.bfsr.client.render;

import net.bfsr.client.texture.Texture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class FrameBuffer {

    private int buffer;
    private int renderBuffer;
    private Texture[] texture;

    public FrameBuffer() {

    }

    public void generate() {
        buffer = GL30.glGenFramebuffers();
    }

    public void bind() {
        OpenGLHelper.bindFrameBuffer(buffer);
    }

    public void generateTexture(int count, int width, int height) {
        bind();
        texture = new Texture[count];
        int[] drawBuffers = new int[count];
        for (int i = 0; i < count; i++) {
            texture[i] = getTexture(i, width, height);
            drawBuffers[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
        }
        GL20.glDrawBuffers(drawBuffers);
        OpenGLHelper.bindFrameBuffer(0);
    }

    public void generateTexture(int width, int height) {
        bind();
        texture = new Texture[1];
        texture[0] = getTexture(0, width, height);
        int[] drawBuffers = new int[]{GL30.GL_COLOR_ATTACHMENT0};
        GL20.glDrawBuffers(drawBuffers);
        OpenGLHelper.bindFrameBuffer(0);
    }

    private Texture getTexture(int i, int width, int height) {
        Texture texture = new Texture(width, height);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

//		float[] borderColor = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
//	    FloatBuffer buf = BufferUtils.createFloatBuffer(4);
//	    buf.put(borderColor);
//	    buf.flip();
//	    glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, buf);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, texture.getId(), 0);
        return texture;
    }

    public void generateRenderBuffer() {
        bind();
        renderBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, texture[0].getWidth(), texture[0].getHeight());
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, renderBuffer);
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
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void viewPort(int width, int height) {
        GL11.glViewport(0, 0, width, height);
    }

    public void deleteTexture(int i) {
        texture[i].delete();
    }

    public void deleteTextures() {
        for (Texture texture : texture)
            texture.delete();
        texture = null;
    }

    public void delete() {
        GL30.glDeleteRenderbuffers(renderBuffer);
        GL30.glDeleteFramebuffers(buffer);
    }

    public void drawBuffer(int i) {
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0 + i);
    }

    public int getBuffer() {
        return buffer;
    }

    public int getRenderBuffer() {
        return renderBuffer;
    }

}

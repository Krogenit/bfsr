package net.bfsr.client.render;

import net.bfsr.client.render.texture.Texture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45C;

public class FrameBuffer {
    private int buffer;
    private int renderBuffer;
    private Texture[] texture;

    public void generate() {
        buffer = GL45C.glCreateFramebuffers();
    }

    public void bind() {
        OpenGLHelper.bindFrameBuffer(buffer);
    }

    public void generateTexture(int count, int width, int height) {
        texture = new Texture[count];
        int[] drawBuffers = new int[count];
        for (int i = 0; i < count; i++) {
            texture[i] = getTexture(i, width, height);
            drawBuffers[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
        }
        GL45C.glNamedFramebufferDrawBuffers(buffer, drawBuffers);
    }

    public void generateTexture(int width, int height) {
        texture = new Texture[1];
        texture[0] = getTexture(0, width, height);
        GL45C.glNamedFramebufferDrawBuffers(buffer, new int[]{GL30.GL_COLOR_ATTACHMENT0});
    }

    private Texture getTexture(int i, int width, int height) {
        Texture texture = new Texture(width, height);
        GL45C.glTextureStorage2D(texture.getId(), 1, GL11.GL_RGB8, width, height);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        GL45C.glNamedFramebufferTexture(buffer, GL30.GL_COLOR_ATTACHMENT0 + i, texture.getId(), 0);
        return texture;
    }

    public void generateRenderBuffer() {
        renderBuffer = GL45C.glCreateRenderbuffers();
        GL45C.glNamedRenderbufferStorage(renderBuffer, GL11.GL_DEPTH_COMPONENT, texture[0].getWidth(), texture[0].getHeight());
        GL45C.glNamedFramebufferRenderbuffer(buffer, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, renderBuffer);
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
        GL45C.glNamedFramebufferDrawBuffer(buffer, GL30.GL_COLOR_ATTACHMENT0 + i);
    }
}

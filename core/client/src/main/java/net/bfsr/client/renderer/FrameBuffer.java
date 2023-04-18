package net.bfsr.client.renderer;

import net.bfsr.client.renderer.texture.Texture;

import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL45C.*;

public class FrameBuffer {
    private int buffer;
    private int renderBuffer;
    private Texture[] texture;

    public void generate() {
        buffer = glCreateFramebuffers();
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, buffer);
    }

    public static void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void generateTexture(int count, int width, int height) {
        texture = new Texture[count];
        int[] drawBuffers = new int[count];
        for (int i = 0; i < count; i++) {
            texture[i] = getTexture(i, width, height);
            drawBuffers[i] = GL_COLOR_ATTACHMENT0 + i;
        }
        glNamedFramebufferDrawBuffers(buffer, drawBuffers);
    }

    public void generateTexture(int width, int height) {
        texture = new Texture[1];
        texture[0] = getTexture(0, width, height);
        glNamedFramebufferDrawBuffers(buffer, new int[]{GL_COLOR_ATTACHMENT0});
    }

    private Texture getTexture(int i, int width, int height) {
        Texture texture = new Texture(width, height).create();
        glTextureStorage2D(texture.getId(), 1, GL_RGB8, width, height);
        glTextureParameteri(texture.getId(), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTextureParameteri(texture.getId(), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTextureParameteri(texture.getId(), GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTextureParameteri(texture.getId(), GL_TEXTURE_WRAP_T, GL_REPEAT);

        glNamedFramebufferTexture(buffer, GL_COLOR_ATTACHMENT0 + i, texture.getId(), 0);
        return texture;
    }

    public void generateRenderBuffer() {
        renderBuffer = glCreateRenderbuffers();
        glNamedRenderbufferStorage(renderBuffer, GL_DEPTH_COMPONENT, texture[0].getWidth(), texture[0].getHeight());
        glNamedFramebufferRenderbuffer(buffer, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);
    }

    public void bindTexture() {
        glBindTexture(GL_TEXTURE_2D, texture[0].getId());
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
        for (int i = 0; i < texture.length; i++) {
            texture[i].delete();
        }

        texture = null;
    }

    public void delete() {
        glDeleteRenderbuffers(renderBuffer);
        glDeleteFramebuffers(buffer);
    }

    public void drawBuffer(int i) {
        glNamedFramebufferDrawBuffer(buffer, GL_COLOR_ATTACHMENT0 + i);
    }
}
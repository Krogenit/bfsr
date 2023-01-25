package net.bfsr.client.render.texture;

import net.bfsr.client.render.OpenGLHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL45C;

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
        this.id = GL45C.glCreateTextures(GL11C.GL_TEXTURE_2D);
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
        GL11.glDeleteTextures(id);
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

package net.bfsr.client.render.texture;

import lombok.Getter;
import net.bfsr.client.render.OpenGLHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL45C;

public class Texture {
    @Getter
    private final int width;
    @Getter
    private final int height;
    @Getter
    private final int id;

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

    public void delete() {
        GL11.glDeleteTextures(id);
    }

    public void bind() {
        OpenGLHelper.bindTexture(id);
    }
}

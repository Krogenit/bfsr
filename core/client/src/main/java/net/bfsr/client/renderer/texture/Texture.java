package net.bfsr.client.renderer.texture;

import lombok.Getter;
import lombok.Setter;

import static org.lwjgl.opengl.ARBBindlessTexture.glMakeTextureHandleNonResidentARB;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL45C.glCreateTextures;

public class Texture {
    @Getter
    protected final int width;
    @Getter
    protected final int height;
    @Getter
    protected int id;
    @Getter
    @Setter
    protected long textureHandle;

    public Texture(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Texture create() {
        this.id = glCreateTextures(GL_TEXTURE_2D);
        return this;
    }

    public void delete() {
        if (textureHandle != 0) glMakeTextureHandleNonResidentARB(textureHandle);
        glDeleteTextures(id);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }
}
package net.bfsr.engine.renderer.texture;

import static org.lwjgl.opengl.ARBBindlessTexture.glMakeTextureHandleNonResidentARB;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL45C.glCreateTextures;

public class Texture extends AbstractTexture {
    public Texture(int width, int height) {
        super(width, height);
    }

    @Override
    public Texture create() {
        this.id = glCreateTextures(GL_TEXTURE_2D);
        return this;
    }

    @Override
    public void delete() {
        if (textureHandle != 0) glMakeTextureHandleNonResidentARB(textureHandle);
        glDeleteTextures(id);
    }

    @Override
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }
}
package net.bfsr.client.renderer.texture;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.opengl.ARBBindlessTexture;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL45C;

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
        this.id = GL45C.glCreateTextures(GL11C.GL_TEXTURE_2D);
        return this;
    }

    public void delete() {
        if (textureHandle != 0) ARBBindlessTexture.glMakeTextureHandleNonResidentARB(textureHandle);
        GL11C.glDeleteTextures(id);
    }
}
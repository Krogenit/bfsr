package net.bfsr.client.font_new;

import org.lwjgl.opengl.GL11;

public class GLListWithTexture {
    public int list;
    public int texture;

    public void clear() {
        GL11.glDeleteLists(list, 1);
    }
}
